/*******************************************************************************
 * Copyright (c) 2023 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.mg;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.CancelableUnitOfWork;
import org.eclipse.xtext.web.server.IServiceContext;
import org.eclipse.xtext.web.server.model.IWebResourceSetProvider;
import org.eclipse.xtext.web.server.model.IXtextWebDocument;
import org.eclipse.xtext.web.server.model.XtextWebDocument;
import org.eclipse.xtext.web.server.model.XtextWebDocumentAccess;
import org.eclipse.xtext.web.server.persistence.IResourceBaseProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MgService {
	@Inject
	private EObjectAtOffsetHelper eObjectAtOffsetHelper;

	private EObject getElementAt(XtextResource resource, int offset) {
		EObject crossLinkedEObject = eObjectAtOffsetHelper.resolveCrossReferencedElementAt(resource, offset);
		if (crossLinkedEObject != null) {
			return crossLinkedEObject;
		} else {
			return eObjectAtOffsetHelper.resolveContainedElementAt(resource, offset);
		}
	}

	@Inject
	private IMg iMg;

	@Inject
	private IResourceBaseProvider resourceBaseProvider;

	@Inject
	private IEncodingProvider encodingProvider;

	@Inject
	private IWebResourceSetProvider resourceSetProvider;

    public Map<String, Object> getArg(IServiceContext context) {
        Map<String, Object> ret = new HashMap<>();
        for (String key : context.getParameterKeys()) {
            String val = context.getParameter(key);
            if (val != null) {
                ret.put(key, val);
            }
        }
        return ret;
    }

    private static int getPos(Map<String, Object> args) {
        Object offset = args.get("offset");
        if (offset instanceof String) {
            try {
                return Integer.parseInt((String) offset);
            } catch (NumberFormatException e) {
                return -2;
            }
        }
        return -1;
    }

    private static int getVersion(IXtextWebDocument doc) {
        return (int) (doc.getResource().getModificationStamp() - Integer.MIN_VALUE);
    }

    // { version, text }
    public MgResult getDocument(XtextWebDocumentAccess document, Map<String, Object> args) {
		return document.readOnly(new CancelableUnitOfWork<MgResult, IXtextWebDocument>() {
			@Override
			public MgResult exec(IXtextWebDocument doc, CancelIndicator cancelIndicator) throws Exception {
                return MgResult.obj("text", doc.getText(),
                                    "version", getVersion(doc),
                                    "dirty", doc.isDirty());
            }
		});
    }

    // { version, text }
    // TODO! clientID
    public MgResult reloadDocument(XtextWebDocumentAccess document, Map<String, Object> args) {
		return document.modify(new CancelableUnitOfWork<MgResult, IXtextWebDocument>() {
			@Override
			public MgResult exec(IXtextWebDocument idoc, CancelIndicator cancelIndicator) throws Exception {
                XtextWebDocument doc = (XtextWebDocument) idoc;
                XtextResource resource = doc.getResource();
                ResourceSet rset = resource.getResourceSet();
                Map<Object, Object> options = rset.getLoadOptions();
                resource.unload();
                resource.load(options);
                String text = doc.setInput(resource);
                resourceSetProvider.updateIndex(doc);
                doc.setDirty(false);
                return MgResult.obj("text", text,
                                    "version", getVersion(doc),
                                    "dirty", false);
            }
		});
    }

    // { version, text }
    // TODO! clientID
    public MgResult saveDocument(XtextWebDocumentAccess document, Map<String, Object> args) {
		return document.modify(new CancelableUnitOfWork<MgResult, IXtextWebDocument>() {
			@Override
            public MgResult exec(IXtextWebDocument idoc, CancelIndicator cancelIndicator) throws Exception {
                XtextWebDocument doc = (XtextWebDocument) idoc;
                URI uri = resourceBaseProvider.getFileURI(doc.getResourceId());
                XtextResource xres = doc.getResource();
                try (OutputStreamWriter writer = new OutputStreamWriter(
					xres.getResourceSet().getURIConverter().createOutputStream(uri),
					encodingProvider.getEncoding(uri))) {
                    writer.write(doc.getText());
                    return MgResult.obj("success", uri.toString());
                } catch (Exception ex) {
                    return MgResult.error(ex.getMessage());
                }
            }
        });
    }

	public MgResult change(XtextWebDocumentAccess document, Map<String, Object> args) {
        Object changeSet = args.get("changeSet");
        if (!(changeSet instanceof String)) {
            return MgResult.error("Invalid ChangeSet:" + changeSet);
        }
        final String json = (String) changeSet;
		return document.modify(new CancelableUnitOfWork<MgResult, IXtextWebDocument>() {
			@Override
			public MgResult exec(IXtextWebDocument doc, CancelIndicator cancelIndicator) throws Exception {
                String ret = iMg.applyChanges(doc, json);
                return new MgResult(ret);
			}
		});
    }

	public MgResult getResult(XtextWebDocumentAccess document, Map<String, Object> args) {
        final int pos = getPos(args);
        if (pos == -2) {
            return MgResult.error("Internal Error: invalid offset in Mg service");
        }

		return document.readOnly(new CancelableUnitOfWork<MgResult, IXtextWebDocument>() {
			@Override
			public MgResult exec(IXtextWebDocument it, CancelIndicator cancelIndicator) throws Exception {
                if (pos > 0) {
                    EObject e = getElementAt(it.getResource(), pos);
                    if (e != null) {
                        args.put("element", e);
                    }
                }
                Object ret = iMg.invoke(args);
				return MgResult.create(ret.toString());
			}
		});
	}
}

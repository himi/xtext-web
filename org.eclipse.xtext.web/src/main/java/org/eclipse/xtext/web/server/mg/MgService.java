/*******************************************************************************
 * Copyright (c) 2023 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.mg;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.CancelableUnitOfWork;
import org.eclipse.xtext.web.server.IServiceContext;
import org.eclipse.xtext.web.server.model.IXtextWebDocument;
import org.eclipse.xtext.web.server.model.XtextWebDocumentAccess;

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

	public MgResult getResult(XtextWebDocumentAccess document, Map<String, Object> args) {
        final int pos = getPos(args);
        if (pos == -2) {
            new MgResult("Internal Error: invalid offset in Mg service");
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
				return new MgResult(ret);
			}
		});
	}
}

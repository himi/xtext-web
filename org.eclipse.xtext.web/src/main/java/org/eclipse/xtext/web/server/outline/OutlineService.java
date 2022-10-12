/*******************************************************************************
 * Copyright (c) 2022 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.CancelableUnitOfWork;
import org.eclipse.xtext.web.server.model.IXtextWebDocument;
import org.eclipse.xtext.web.server.model.XtextWebDocumentAccess;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OutlineService {
	private INode getNodeAt(XtextResource resource, int offset) {
        IParseResult parseResult = resource.getParseResult();
        if (parseResult == null) return null;
        INode rootNode = parseResult.getRootNode();
        ILeafNode leaf = NodeModelUtils.findLeafNodeAtOffset(rootNode, offset);
        if (leaf == null) return rootNode;
        return leaf;
    }


	@Inject
	private IOutliner outliner;

	public OutlineResult getResult(XtextWebDocumentAccess document, int offset) {
		return document.readOnly(new CancelableUnitOfWork<OutlineResult, IXtextWebDocument>() {
			@Override
			public OutlineResult exec(IXtextWebDocument it, CancelIndicator cancelIndicator) throws Exception {
                List<INode> nodes = new ArrayList<>();
                XtextResource xres = it.getResource();
                if (offset >= 0) {
                    INode node = getNodeAt(xres, offset);
                    if (node == null) return OutlineResult.emptyResult();
                    nodes.add(node);
                } else {
                    for (EObject eObj : xres.getContents()) {
                        INode node = NodeModelUtils.findActualNodeFor(eObj);
                        if (node != null) {
                            nodes.add(node);
                        }
                    }
                }
                OutlineElement[] result = outliner.toOutline(nodes);
				return new OutlineResult(result);
			}
		});
	}
}

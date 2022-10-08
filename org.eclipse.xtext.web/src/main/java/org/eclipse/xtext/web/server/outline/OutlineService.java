/*******************************************************************************
 * Copyright (c) 2022 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.outline;

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
				INode node = getNodeAt(it.getResource(), offset);
                OutlineElement[] result = outliner.toOutline(node);
				return new OutlineResult(result);
			}
		});
	}
}

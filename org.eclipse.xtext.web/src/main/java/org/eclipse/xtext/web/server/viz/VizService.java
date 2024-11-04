/*******************************************************************************
 * Copyright (c) 2022 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.viz;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.CancelableUnitOfWork;
import org.eclipse.xtext.web.server.model.IXtextWebDocument;
import org.eclipse.xtext.web.server.model.XtextWebDocumentAccess;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class VizService {
	@Inject
	private EObjectAtOffsetHelper eObjectAtOffsetHelper;

	private EObject getElementAt(XtextResource resource, int offset) {
        return eObjectAtOffsetHelper.resolveElementAt(resource, offset);
        /*
		EObject crossLinkedEObject = resolveCrossLinkedElementAt(resource, offset);
		if (crossLinkedEObject != null) {
			return crossLinkedEObject;
		} else {
			return eObjectAtOffsetHelper.resolveContainedElementAt(resource, offset);
		}
        */
	}

	@Inject
	private IVisualizer visualizer;

	public VizResult getResult(XtextWebDocumentAccess document, int offset, String view, String styles) {
		return document.readOnly(new CancelableUnitOfWork<VizResult, IXtextWebDocument>() {
			@Override
			public VizResult exec(IXtextWebDocument it, CancelIndicator cancelIndicator) throws Exception {
				EObject element = getElementAt(it.getResource(), offset);
                if (element == null) {
                    return new VizResult("<svg xmlns='http://www.w3.org/2000/svg' width='8' height='8' viewBox='0 0 8 8'><path d='M4 0c-2.2 0-4 1.8-4 4s1.8 4 4 4 4-1.8 4-4-1.8-4-4-4zm0 1c.66 0 1.26.21 1.75.56l-4.19 4.19c-.35-.49-.56-1.09-.56-1.75 0-1.66 1.34-3 3-3zm2.44 1.25c.35.49.56 1.09.56 1.75 0 1.66-1.34 3-3 3-.66 0-1.26-.21-1.75-.56l4.19-4.19z' /></svg>");
                }
                String svg = visualizer.toSVG(element, view, styles);
				return new VizResult(svg);
			}
		});
	}
}

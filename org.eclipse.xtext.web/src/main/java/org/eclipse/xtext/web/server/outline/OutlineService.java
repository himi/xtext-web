/*******************************************************************************
 * Copyright (c) 2022 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.outline;

import org.eclipse.xtext.web.server.model.XtextWebDocumentAccess;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OutlineService {
	@Inject
	private IOutliner outliner;

	public OutlineResult getResult(XtextWebDocumentAccess document, int offset) {
        return new OutlineResult(outliner.toOutline(document, offset));
	}
}

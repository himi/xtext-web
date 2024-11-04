/*******************************************************************************
 * Copyright (c) 2023 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.mg;

import java.util.Map;

import org.eclipse.xtext.web.server.model.IXtextWebDocument;

public interface IMg {
    String applyChanges(IXtextWebDocument doc, String json);
    Object invoke(Map<String, Object> args);
}

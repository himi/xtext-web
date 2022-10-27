/*******************************************************************************
 * Copyright (c) 2022 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.viz;

import org.eclipse.emf.ecore.EObject;

public interface IVisualizer {
    String toSVG(EObject eObj, String view, String styles);
}

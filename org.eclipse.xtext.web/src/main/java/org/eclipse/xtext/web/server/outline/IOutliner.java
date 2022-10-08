/*******************************************************************************
 * Copyright (c) 2022 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.outline;

import org.eclipse.xtext.nodemodel.INode;

public interface IOutliner {
   OutlineElement[] toOutline(INode node);
}

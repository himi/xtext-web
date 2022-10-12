/*******************************************************************************
 * Copyright (c) 2022 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.outline;

import java.util.List;

import org.eclipse.xtext.nodemodel.INode;

public interface IOutliner {
   OutlineElement[] toOutline(List<INode> nodes);
}

/*******************************************************************************
 * Copyright (c) 2023 Mgnite Inc. All rights reserved.
 *
 * Author:
 *    Hisashi Miyashita <himi@mgnite.com>
 *******************************************************************************/

package org.eclipse.xtext.web.server.mg;

import java.util.Map;

public interface IMg {
    Object invoke(Map<String, Object> args);
}

/* 
 * Sourcerer: an infrastructure for large-scale source code analysis.
 * Copyright (C) by contributors. See CONTRIBUTORS.txt for full list.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Joel Ossher (jossher@uci.edu)
 */

// BEGIN TEST

// CLASS public *pkg*.ClassExtendsInnerClass public }
// INSIDE *pkg*.ClassExtendsInnerClass *pkg*
// EXTENDS *pkg*.ClassExtendsInnerClass edu.uci.ics.sourcerer.extractor.test.ClassType$Inner ClassType.Inner
// USES *pkg*.ClassExtendsInnerClass edu.uci.ics.sourcerer.extractor.test.ClassType ClassType
// USES *pkg*.ClassExtendsInnerClass edu.uci.ics.sourcerer.extractor.test.ClassType$Inner Inner

// CONSTRUCTOR public *pkg*.ClassExtendsInnerClass.<init>()
// INSIDE *pkg*.ClassExtendsInnerClass.<init>() *pkg*.ClassExtendsInnerClass
// CALLS *pkg*.ClassExtendsInnerClass.<init>() edu.uci.ics.sourcerer.extractor.test.ClassType$Inner.<init>() -
package edu.uci.ics.sourcerer.extractor.test.relation.extends_;

import edu.uci.ics.sourcerer.extractor.test.ClassType;

public class ClassExtendsInnerClass extends ClassType.Inner {

}

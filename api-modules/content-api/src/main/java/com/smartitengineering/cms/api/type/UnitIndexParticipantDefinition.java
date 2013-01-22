/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2013  Imran M Yousuf (imyousuf@smartitengineering.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.smartitengineering.cms.api.type;

import java.util.Comparator;

/**
 * A combination of unit index participant. It is defined by field name and index of the field in the index key.
 * @author imyousuf
 */
public interface UnitIndexParticipantDefinition extends Comparator<UnitIndexParticipantDefinition> {

  /**
   * Retrieve the name of the field in the unit definition; where the string is delimited by '.'.
   * @return The name of the field delimited by '.'
   */
  String getFieldName();

  /**
   * Retrieve the participant index.
   * @return The participant index.
   */
  int getIndex();
}

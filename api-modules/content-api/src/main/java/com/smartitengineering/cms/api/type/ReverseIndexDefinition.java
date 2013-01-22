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

import java.util.Collection;

/**
 * Defines a single reverse index or secondary key for a given content type.
 * @author imyousuf
 */
public interface ReverseIndexDefinition {

  /**
   * Retrieve the collection of unit index participant definition
   * @return The participants
   */
  Collection<UnitIndexParticipantDefinition> getIndexParticipants();

  /**
   * Retrieve the name of the index.
   * @return Name of the index
   */
  String getName();
}

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

/**
 * A mutable reverse index definition
 * @author imyousuf
 */
public interface MutableReverseIndexDefinition extends ReverseIndexDefinition {

  /**
   * Remove participant from index by matching both its name and index value.
   * @param def The definition to remove.
   */
  public void removeParticipant(UnitIndexParticipantDefinition def);

  /**
   * Add a participant to the index after verifying that it fulfills the conditions of have unique index key and unique
   * field name. If not then it will be rejected by throwing IllegalArgumentException.
   * @param def The participant to add to index
   * @throws IllegalArgumentException If either key or field name is not unique.
   */
  public void addParticipant(UnitIndexParticipantDefinition def) throws IllegalArgumentException;

  /**
   * Sets the name of the index
   * @param name Name of the index
   */
  public void setName(String name);
}

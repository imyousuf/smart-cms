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
package com.smartitengineering.cms.api.impl.type;

import com.smartitengineering.cms.api.type.MutableReverseIndexDefinition;
import com.smartitengineering.cms.api.type.UnitIndexParticipantDefinition;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Implementation of the reverse index definition
 * @author imyousuf
 */
public class ReverseIndexDefinitionImpl implements MutableReverseIndexDefinition {

  private String name;
  private final SortedSet<UnitIndexParticipantDefinition> definitions = new TreeSet<UnitIndexParticipantDefinition>();

  public void removeParticipant(UnitIndexParticipantDefinition def) {
    definitions.remove(def);
  }

  public void addParticipant(UnitIndexParticipantDefinition def) throws IllegalArgumentException {
    if (definitions.contains(def)) {
      throw new IllegalArgumentException("Similar instance is already a participant");
    }
    for (UnitIndexParticipantDefinition defn : definitions) {
      if (def.getFieldName().equals(defn.getFieldName())) {
        throw new IllegalArgumentException("Same field name already a participant");
      }
      if (def.getIndex() == defn.getIndex()) {
        throw new IllegalArgumentException("Index already occupied by another participant");
      }
    }
    definitions.add(def);
  }

  public void setName(String name) {
    this.name = name;
  }

  public Collection<UnitIndexParticipantDefinition> getIndexParticipants() {
    return Collections.unmodifiableSortedSet(definitions);
  }

  public String getName() {
    return this.name;
  }
}

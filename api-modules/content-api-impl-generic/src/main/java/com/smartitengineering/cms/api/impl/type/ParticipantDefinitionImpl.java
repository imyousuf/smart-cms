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

import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.UnitIndexParticipantDefinition;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author imyousuf
 */
public class ParticipantDefinitionImpl implements UnitIndexParticipantDefinition {

  private final String fieldName;
  private final int index;

  public ParticipantDefinitionImpl(FieldDef def, int index) {
    if (def == null) {
      throw new IllegalArgumentException("Field definition can not be null");
    }
    this.index = index;
    this.fieldName = getNameFromDefinition(def);
  }

  private String getNameFromDefinition(FieldDef def) {
    if (def == null) {
      return "";
    }
    String parent = getNameFromDefinition(def.getParentContainer());
    if (StringUtils.isNotBlank(parent)) {
      return new StringBuilder(parent).append('.').append(def.getName()).toString();
    }
    return def.getName();
  }

  public String getFieldName() {
    return fieldName;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final UnitIndexParticipantDefinition other = (UnitIndexParticipantDefinition) obj;
    if ((this.fieldName == null) ? (other.getFieldName() != null) : !this.fieldName.equals(other.getFieldName())) {
      return false;
    }
    if (this.index != other.getIndex()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + (this.fieldName != null ? this.fieldName.hashCode() : 0);
    hash = 53 * hash + this.index;
    return hash;
  }

  public int compare(UnitIndexParticipantDefinition o1, UnitIndexParticipantDefinition o2) {
    int c2 = o1.getFieldName().compareTo(o2.getFieldName());
    if (c2 == 0) {
      return new Integer(o1.getIndex()).compareTo(new Integer(o2.getIndex()));
    }
    else {
      return c2;
    }
  }
}

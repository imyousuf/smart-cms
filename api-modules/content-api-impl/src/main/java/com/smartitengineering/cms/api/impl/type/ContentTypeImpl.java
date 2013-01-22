/*
 *
 * This is a simple Content Management System (CMS)
 * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
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

import com.smartitengineering.cms.api.common.MediaType;
import com.smartitengineering.cms.api.event.Event;
import com.smartitengineering.cms.api.event.Event.EventType;
import com.smartitengineering.cms.api.event.Event.Type;
import com.smartitengineering.cms.api.factory.SmartContentAPI;
import com.smartitengineering.cms.api.factory.type.WritableContentType;
import com.smartitengineering.cms.api.impl.AbstractPersistableDomain;
import com.smartitengineering.cms.api.type.ContentCoProcessorDef;
import com.smartitengineering.cms.api.type.ContentStatus;
import com.smartitengineering.cms.api.type.ContentType;
import com.smartitengineering.cms.api.type.ContentTypeId;
import com.smartitengineering.cms.api.type.FieldDef;
import com.smartitengineering.cms.api.type.MutableReverseIndexDefinition;
import com.smartitengineering.cms.api.type.RepresentationDef;
import com.smartitengineering.cms.api.type.ReverseIndexDefinition;
import com.smartitengineering.cms.spi.type.PersistableContentType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kaisar
 */
public class ContentTypeImpl extends AbstractPersistableDomain<WritableContentType, ContentTypeId> implements
    PersistableContentType {

  private ContentTypeId contentTypeId;
  private final Set<ContentStatus> contentStatus = new LinkedHashSet<ContentStatus>();
  private final Set<FieldDef> fieldDefs = new LinkedHashSet<FieldDef>();
  private final Set<RepresentationDef> representationDefs = new LinkedHashSet<RepresentationDef>();
  private ContentTypeId parentTypeId;
  private String displayName;
  private String entityTagValue;
  private Date creationDate;
  private Date lastModifiedDate;
  private boolean fromPersistentStorage;
  private String primaryFieldName;
  private final Map<MediaType, String> representations = new HashMap<MediaType, String>();
  private final Map<String, String> parameterizedDisplayNames = new LinkedHashMap<String, String>();
  private final Map<ContentProcessingPhase, Collection<ContentCoProcessorDef>> procDefs =
                                                                               new EnumMap<ContentProcessingPhase, Collection<ContentCoProcessorDef>>(
      ContentProcessingPhase.class);
  private final Map<String, ReverseIndexDefinition> reverseIndexDefinitions =
                                                    new HashMap<String, ReverseIndexDefinition>();
  private DefinitionType definitionType;

  public ContentTypeImpl() {
    super(LOCK_KEY_PREFIX);
  }

  @Override
  public void put() throws IOException {
    if (!isValid()) {
      getLogger().info("Content type not in valid state!");
      //First get contents indexed before attempting to use this validity!
      //Uncomment the following line once indexing is ensured in testing
      throw new IOException("Content is not in valid state!");
    }
    super.put();
  }

  @Override
  public void setContentTypeID(ContentTypeId contentTypeID) throws IllegalArgumentException {
    if (contentTypeID != null) {
      this.contentTypeId = contentTypeID;
    }
  }

  @Override
  public Collection<ContentStatus> getMutableStatuses() {
    return this.contentStatus;
  }

  @Override
  public Collection<FieldDef> getMutableFieldDefs() {
    return this.fieldDefs;
  }

  @Override
  public ContentTypeId getContentTypeID() {
    return this.contentTypeId;
  }

  @Override
  public Map<String, ContentStatus> getStatuses() {
    final Map<String, ContentStatus> statusMap = new LinkedHashMap<String, ContentStatus>();
    if (contentStatus.isEmpty() && getParent() != null) {
      ContentType parentContentType = getParent().getContentType();
      if (parentContentType != null) {
        return parentContentType.getStatuses();
      }
      else {
        return Collections.emptyMap();
      }
    }
    else if (!contentStatus.isEmpty()) {
      for (ContentStatus status : contentStatus) {
        statusMap.put(status.getName(), status);
      }
      return Collections.unmodifiableMap(statusMap);
    }
    else {
      final ContentStatus status = new ContentStatus() {

        public int getId() {
          return 1;
        }

        public ContentTypeId getContentType() {
          return getContentTypeID();
        }

        public String getName() {
          return "default";
        }
      };
      statusMap.put(status.getName(), status);
      return Collections.unmodifiableMap(statusMap);
    }
  }

  @Override
  public Map<String, FieldDef> getFieldDefs() {
    Map<String, FieldDef> fieldDefMap = new LinkedHashMap<String, FieldDef>();
    if (parentTypeId != null) {
      ContentType parantType = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(parentTypeId);
      if (parantType != null) {
        fieldDefMap.putAll(parantType.getFieldDefs());
      }
    }
    fieldDefMap.putAll(getOwnFieldDefs());
    return Collections.unmodifiableMap(fieldDefMap);
  }

  @Override
  public Map<String, FieldDef> getOwnFieldDefs() {
    Map<String, FieldDef> fieldDefMap = new LinkedHashMap<String, FieldDef>(fieldDefs.size());
    for (FieldDef fieldDef : fieldDefs) {
      fieldDefMap.put(fieldDef.getName(), fieldDef);
    }
    return Collections.unmodifiableMap(fieldDefMap);
  }

  @Override
  public Map<String, RepresentationDef> getRepresentationDefs() {
    Map<String, RepresentationDef> representationDefMap = new LinkedHashMap<String, RepresentationDef>();
    if (getParent() != null) {
      ContentType parentContentType = getParent().getContentType();
      if (parentContentType != null) {
        representationDefMap.putAll(parentContentType.getRepresentationDefs());
      }
    }
    for (RepresentationDef representationDef : representationDefs) {
      representationDefMap.put(representationDef.getName(), representationDef);
    }
    return Collections.unmodifiableMap(representationDefMap);

  }

  @Override
  public ContentTypeId getParent() {
    return this.parentTypeId;
  }

  @Override
  public String getDisplayName() {
    return this.displayName;
  }

  @Override
  public Date getCreationDate() {
    return this.creationDate;

  }

  @Override
  public Date getLastModifiedDate() {
    return this.lastModifiedDate;

  }

  @Override
  public boolean isFromPersistentStorage() {
    return fromPersistentStorage;
  }

  @Override
  public void setFromPersistentStorage(boolean fromPersistentStorage) {
    this.fromPersistentStorage = fromPersistentStorage;
  }

  @Override
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  @Override
  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  @Override
  public boolean isPersisted() {
    return isFromPersistentStorage();
  }

  @Override
  public ContentTypeId getKeySpecimen() {
    return contentTypeId;
  }

  @Override
  public Collection<RepresentationDef> getMutableRepresentationDefs() {
    return representationDefs;
  }

  @Override
  public void setParent(ContentTypeId parentId) {
    this.parentTypeId = parentId;
  }

  @Override
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!ContentType.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final ContentType other = (ContentType) obj;
    if (this.contentTypeId != other.getContentTypeID() && (this.contentTypeId == null ||
                                                           !this.contentTypeId.equals(other.getContentTypeID()))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 61 * hash + (this.contentTypeId != null ? this.contentTypeId.hashCode() : 0);
    return hash;
  }

  @Override
  public void setRepresentations(Map<MediaType, String> reps) {
    representations.clear();
    if (reps == null || reps.isEmpty()) {
      return;
    }
    else {
      representations.putAll(reps);
    }
  }

  @Override
  public Map<MediaType, String> getRepresentations() {
    return Collections.unmodifiableMap(representations);
  }

  @Override
  public void setEntityTagValue(String entityTagValue) {
    this.entityTagValue = entityTagValue;
  }

  @Override
  public String getEntityTagValue() {
    return entityTagValue;
  }

  @Override
  public RepresentationDef getRepresentationDefForMimeType(String mimeType) {
    TreeMap<String, RepresentationDef> map = new TreeMap<String, RepresentationDef>();
    for (RepresentationDef def : representationDefs) {
      if (def.getMIMEType().equals(mimeType)) {
        map.put(def.getName(), def);
      }
    }
    if (map.isEmpty()) {
      return null;
    }
    else {
      return map.firstEntry().getValue();
    }
  }

  @Override
  public void setPrimaryFieldName(String primaryFieldName) {
    this.primaryFieldName = primaryFieldName;
  }

  @Override
  public FieldDef getPrimaryFieldDef() {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Trying to get primary field for type " + getContentTypeID().toString());
    }
    if (StringUtils.isBlank(primaryFieldName)) {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Trying to get primary field from parent!");
        getLogger().debug("Parent type id " + parentTypeId);
      }
      if (parentTypeId != null) {
        ContentType parantType = SmartContentAPI.getInstance().getContentTypeLoader().loadContentType(parentTypeId);
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("Parent type " + parantType);
        }
        if (parantType != null) {
          if (getLogger().isDebugEnabled()) {
            getLogger().debug("Parent primary field name: " + parantType.getPrimaryFieldName());
          }
          return parantType.getPrimaryFieldDef();
        }
      }
      return null;
    }
    else {
      return getFieldDefs().get(primaryFieldName);
    }
  }

  private boolean isValid() {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Primary Field Def present: " + primaryFieldName + " " + (getPrimaryFieldDef() != null));
      getLogger().debug("Primary Field Def: " + getPrimaryFieldDef());
    }
    final DefinitionType myDefType = getDefinitionType();
    if (getParent() != null && getParent().getContentType() != null) {
      DefinitionType parentType = getParent().getContentType().getDefinitionType();
      if (parentType.equals(DefinitionType.ABSTRACT_COMPONENT) || parentType.equals(DefinitionType.CONCRETE_COMPONENT)) {
        if (myDefType.equals(DefinitionType.ABSTRACT_TYPE) || myDefType.equals(DefinitionType.CONCRETE_TYPE)) {
          getLogger().warn("Parent definition is of component, but this is of type!");
          return false;
        }
      }
      else {
        if (myDefType.equals(DefinitionType.ABSTRACT_COMPONENT) || myDefType.equals(DefinitionType.CONCRETE_COMPONENT)) {
          getLogger().warn("Parent definition is of type, but this is of component!");
          return false;
        }
      }
    }
    if (myDefType.equals(DefinitionType.ABSTRACT_COMPONENT) || myDefType.equals(DefinitionType.ABSTRACT_TYPE)) {
      return true;
    }
    return getPrimaryFieldDef() != null && !getStatuses().isEmpty();
  }

  @Override
  public String getPrimaryFieldName() {
    return primaryFieldName;
  }

  @Override
  protected void create() throws IOException {
    super.create();
    Event<ContentType> contentEvent = SmartContentAPI.getInstance().getEventRegistrar().<ContentType>createEvent(
        EventType.CREATE, Type.CONTENT_TYPE, this);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEvent(contentEvent);
  }

  @Override
  public void delete() throws IOException {
    super.delete();
    Event<ContentType> contentEvent = SmartContentAPI.getInstance().getEventRegistrar().<ContentType>createEvent(
        EventType.DELETE, Type.CONTENT_TYPE, this);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEvent(contentEvent);
  }

  @Override
  protected void update() throws IOException {
    super.update();
    Event<ContentType> contentEvent = SmartContentAPI.getInstance().getEventRegistrar().<ContentType>createEvent(
        EventType.UPDATE, Type.CONTENT_TYPE, this);
    SmartContentAPI.getInstance().getEventRegistrar().notifyEvent(contentEvent);
  }

  public void setParameterizedDisplayNames(Map<String, String> params) {
    this.parameterizedDisplayNames.clear();
    if (params != null && !params.isEmpty()) {
      this.parameterizedDisplayNames.putAll(params);
    }
  }

  public Map<String, String> getMutableParameterizedDisplayNames() {
    return this.parameterizedDisplayNames;
  }

  public Map<String, String> getParameterizedDisplayNames() {
    return Collections.unmodifiableMap(parameterizedDisplayNames);
  }

  public void setDefinitionType(DefinitionType type) {
    this.definitionType = type;
  }

  public DefinitionType getDefinitionType() {
    if (definitionType != null) {
      return definitionType;
    }
    else {
      if (getParent() == null || getParent().getContentType() == null) {
        return DefinitionType.getDefaufltType();
      }
      else {
        ContentType parent = getParent().getContentType();
        final DefinitionType parentDefType = parent.getDefinitionType();
        if (parentDefType.equals(DefinitionType.ABSTRACT_COMPONENT) || parentDefType.equals(
            DefinitionType.CONCRETE_COMPONENT)) {
          return DefinitionType.CONCRETE_COMPONENT;
        }
        else if (parentDefType.equals(DefinitionType.ABSTRACT_TYPE) ||
            parentDefType.equals(DefinitionType.CONCRETE_TYPE)) {
          return DefinitionType.CONCRETE_TYPE;
        }
        else {
          return DefinitionType.getDefaufltType();
        }
      }
    }
  }

  public DefinitionType getSelfDefinitionType() {
    return definitionType;
  }

  public Map<ContentProcessingPhase, Collection<ContentCoProcessorDef>> getMutableContentCoProcessorDefs() {
    return procDefs;
  }

  public Map<ContentProcessingPhase, Collection<ContentCoProcessorDef>> getContentCoProcessorDefs() {
    final EnumMap<ContentProcessingPhase, Collection<ContentCoProcessorDef>> newMap =
                                                                             new EnumMap<ContentProcessingPhase, Collection<ContentCoProcessorDef>>(
        ContentProcessingPhase.class);
    newMap.putAll(procDefs);
    return newMap;
  }

  public void addContentCoProcessorDef(ContentCoProcessorDef def) {
    ContentProcessingPhase phase = def.getPhase();
    if (phase == null) {
      throw new NullPointerException();
    }
    Collection<ContentCoProcessorDef> collection = procDefs.get(phase);
    if (collection == null) {
      collection = new ArrayList<ContentCoProcessorDef>();
      procDefs.put(phase, collection);
    }
    collection.add(def);
  }

  public void removeContentCoProcessorDef(ContentCoProcessorDef def) {
    ContentProcessingPhase phase = def.getPhase();
    if (phase == null) {
      throw new NullPointerException();
    }
    Collection<ContentCoProcessorDef> collection = procDefs.get(phase);
    if (collection == null) {
      return;
    }
    collection.remove(def);
  }

  public void addReverseIndexDefintion(MutableReverseIndexDefinition def) {
    if (def != null && StringUtils.isNotBlank(def.getName())) {
      reverseIndexDefinitions.put(def.getName(), def);
    }
  }

  public void removeReverseIndexDefintion(MutableReverseIndexDefinition def) {
    if (def != null && StringUtils.isNotBlank(def.getName())) {
      reverseIndexDefinitions.remove(def.getName());
    }
  }

  public Map<String, ReverseIndexDefinition> getReverseIndexDefinitions() {
    return Collections.unmodifiableMap(reverseIndexDefinitions);
  }
}

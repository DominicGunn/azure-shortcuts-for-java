/**
* Copyright (c) Microsoft Corporation
* 
* All rights reserved. 
* 
* MIT License
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
* (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
* ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
* THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.microsoft.azure.shortcuts.resources.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.shortcuts.resources.ResourceGroup;
import com.microsoft.azure.shortcuts.resources.ResourceGroups;
import com.microsoft.azure.shortcuts.resources.Region;

public class ResourceGroupsImpl 
	extends EntitiesImpl<Subscription>
	implements ResourceGroups {
	
	ResourceGroupsImpl(Subscription subscription) {
		super(subscription);
	}
	
	
	@Override
	public Map<String, ResourceGroup> asMap() throws Exception {
		HashMap<String, ResourceGroup> wrappers = new HashMap<>();
		for(ResourceGroupExtended nativeItem : getNativeEntities()) {
			ResourceGroupImpl wrapper = new ResourceGroupImpl(nativeItem, this);
			wrappers.put(nativeItem.getName(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

		
	@Override
	// Gets a specific resource group
	public ResourceGroupImpl get(String name) throws Exception {
		ResourceGroupExtended azureGroup = azure.resourceManagementClient().getResourceGroupsOperations().get(name).getResourceGroup();
		return new ResourceGroupImpl(azureGroup, this);
	}
	
	
	@Override
	public void delete(String name) throws Exception {
		azure.resourceManagementClient().getResourceGroupsOperations().delete(name);
		//TODO: Apparently the effect of the deletion is not immediate - Azure SDK misleadingly returns from this synch call even though listing resource groups will still include this
	}
	

	@Override
	public ResourceGroupImpl update(String name) {
		return createWrapper(name);
	}


	@Override
	public ResourceGroupImpl define(String name) {
		return createWrapper(name);
	}

	
	/***************************************************
	 * Helpers
	 ***************************************************/
	
	// Wraps native Azure group
	private ResourceGroupImpl createWrapper(String name) {
		ResourceGroupExtended azureGroup = new ResourceGroupExtended();
		azureGroup.setName(name);
		return new ResourceGroupImpl(azureGroup, this);
		
	}
	
	// Helper to get the resource groups from Azure
	private ArrayList<ResourceGroupExtended> getNativeEntities() throws Exception {
		return this.azure.resourceManagementClient().getResourceGroupsOperations().list(null).getResourceGroups();		
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class ResourceGroupImpl 
		extends 
			IndexableRefreshableWrapperImpl<ResourceGroup, ResourceGroupExtended>
		implements
			ResourceGroup.Update,
			ResourceGroup.DefinitionProvisionable,
			ResourceGroup.DefinitionBlank,
			ResourceGroup {
		
		private final EntitiesImpl<Subscription> collection;
		
		private ResourceGroupImpl(ResourceGroupExtended azureGroup, EntitiesImpl<Subscription> collection) {
			super(azureGroup.getName(), azureGroup);
			this.collection = collection;
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/
		
		@Override
		public String region() throws Exception {
			return this.inner().getLocation();
		}

		@Override
		public Map<String, String> tags() throws Exception {
			return Collections.unmodifiableMap(this.inner().getTags());
		}

		@Override
		public String provisioningState() throws Exception {
			return this.inner().getProvisioningState();
		}

		@Override
		public String name() {
			return this.inner().getName();
		}
		
		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/
		
		@Override
		public ResourceGroupImpl withTags(Map<String, String> tags) {
			this.inner().setTags(new HashMap<>(tags));
			return this;
		}

		@Override
		public ResourceGroupImpl withTag(String key, String value) {
			if(this.inner().getTags() == null) {
				this.inner().setTags(new HashMap<String, String>());
			}
			this.inner().getTags().put(key, value);
			return this;
		}

		@Override
		public ResourceGroupImpl withoutTag(String key) {
			this.inner().getTags().remove(key);
			return this;
		}

		@Override
		public ResourceGroupImpl withRegion(String regionName) {
			this.inner().setLocation(regionName);
			return this;
		}
		
		@Override
		public ResourceGroupImpl withRegion(Region region) {
			return this.withRegion(region.toString());
		}


		/************************************************************
		 * Verbs
		 ************************************************************/
		
		@Override
		public ResourceGroupImpl apply() throws Exception {
			com.microsoft.azure.management.resources.models.ResourceGroup params = 
				new com.microsoft.azure.management.resources.models.ResourceGroup();
			ResourceGroup group;
			
			params.setTags(this.inner().getTags());
			
			// Figure out the region, since the SDK requires on the params explicitly even though it cannot be changed
			if(this.inner().getLocation() != null) {
				params.setLocation(this.inner().getLocation());
			} else if(null == (group = azure.resourceGroups().get(this.id))) {
				throw new Exception("Resource group not found");
			} else {
				params.setLocation(group.region());
			}

			this.collection.azure().resourceManagementClient().getResourceGroupsOperations().createOrUpdate(this.id, params);
			return this;
		}

		
		@Override
		public void delete() throws Exception {
			this.collection.azure().resourceGroups().delete(this.id);
		}

		
		@Override
		public ResourceGroupImpl provision() throws Exception {
			com.microsoft.azure.management.resources.models.ResourceGroup params = 
				new com.microsoft.azure.management.resources.models.ResourceGroup();
			params.setLocation(this.inner().getLocation());
			params.setTags(this.inner().getTags());
			this.collection.azure().resourceManagementClient().getResourceGroupsOperations().createOrUpdate(this.id, params);
			return this;
		}

		
		@Override
		public ResourceGroupImpl refresh() throws Exception {
			this.setInner(this.collection.azure().resourceManagementClient().getResourceGroupsOperations().get(this.id).getResourceGroup());
			return this;
		}
	}
}

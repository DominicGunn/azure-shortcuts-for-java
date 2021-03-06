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
import java.util.List;

import com.microsoft.azure.management.network.models.SecurityRule;
import com.microsoft.azure.management.network.models.SecurityRuleAccess;
import com.microsoft.azure.management.network.models.SecurityRuleDirection;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.resources.NetworkSecurityGroup;
import com.microsoft.azure.shortcuts.resources.NetworkSecurityGroups;
import com.microsoft.azure.shortcuts.resources.Protocol;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourceBaseImpl;
import com.microsoft.azure.shortcuts.resources.common.implementation.GroupableResourcesBaseImpl;


public class NetworkSecurityGroupsImpl 
	extends GroupableResourcesBaseImpl<
		NetworkSecurityGroup, 
		com.microsoft.azure.management.network.models.NetworkSecurityGroup,
		NetworkSecurityGroupsImpl.NetworkSecurityGroupImpl>
	implements NetworkSecurityGroups {
		
	NetworkSecurityGroupsImpl(Subscription subscription) {
		super(subscription);
	}
	
	@Override
	public NetworkSecurityGroupImpl define(String name) throws Exception {
		com.microsoft.azure.management.network.models.NetworkSecurityGroup nativeItem = 
				new com.microsoft.azure.management.network.models.NetworkSecurityGroup();
		nativeItem.setName(name);
		nativeItem.setSecurityRules(new ArrayList<SecurityRule>());
		return wrap(nativeItem);
	}

	@Override
	public void delete(String groupName, String name) throws Exception {
		azure.networkManagementClient().getNetworkSecurityGroupsOperations().delete(groupName, name);
	}


	/***************************************************
	 * Helpers
	 ***************************************************/
	
	@Override
	protected List<com.microsoft.azure.management.network.models.NetworkSecurityGroup> getNativeEntities(String resourceGroupName) throws Exception {
		if(resourceGroupName == null) {
			return this.azure.networkManagementClient().getNetworkSecurityGroupsOperations().listAll().getNetworkSecurityGroups();
		} else {
			return this.azure.networkManagementClient().getNetworkSecurityGroupsOperations().list(resourceGroupName).getNetworkSecurityGroups();
		}
	}
	
	@Override
	protected com.microsoft.azure.management.network.models.NetworkSecurityGroup getNativeEntity(String groupName, String name) throws Exception {
		return azure.networkManagementClient().getNetworkSecurityGroupsOperations().get(groupName, name).getNetworkSecurityGroup();
	}
	
	@Override
	protected NetworkSecurityGroupImpl wrap(com.microsoft.azure.management.network.models.NetworkSecurityGroup nativeItem) {
		return new NetworkSecurityGroupImpl(nativeItem, this);
	}
	
	
	/***************************************************************
	 * Implements logic for individual NSG
	 ***************************************************************/
	class NetworkSecurityGroupImpl 
		extends 
			GroupableResourceBaseImpl<
				NetworkSecurityGroup, 
				com.microsoft.azure.management.network.models.NetworkSecurityGroup,
				NetworkSecurityGroupImpl>
		implements
			NetworkSecurityGroup,
			NetworkSecurityGroup.DefinitionBlank,
			NetworkSecurityGroup.DefinitionWithGroup,
			NetworkSecurityGroup.DefinitionProvisionable {
		
		private NetworkSecurityGroupImpl(com.microsoft.azure.management.network.models.NetworkSecurityGroup azureItem, 
				EntitiesImpl<Subscription> collection) {
			super(azureItem.getName(), azureItem, collection);
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/

		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/

		/************************************************************
		 * Verbs
		 ************************************************************/

		@Override
		public void delete() throws Exception {
			this.collection.azure().networkSecurityGroups().delete(this.id());
		}

		@Override
		public NetworkSecurityGroup provision() throws Exception {
			// Create a group as needed
			ensureGroup();
		
			this.collection.azure().networkManagementClient().getNetworkSecurityGroupsOperations().createOrUpdate(this.groupName, this.name(), this.inner());
			return get(this.groupName, this.name());
		}
		
		@Override
		public NetworkSecurityGroup refresh() throws Exception {
			this.setInner(getNativeEntity(
					ResourcesImpl.groupFromResourceId(this.id()), 
					ResourcesImpl.nameFromResourceId(this.id())));
			return this;
		}

		
		private SecurityRule createSecurityRule(
			String direction,
			String access,
			Protocol protocol,
			String sourceAddressPrefix,
			String sourcePortRange,
			String destinationAddressPrefix,
			String destinationPortRange,
			String name,
			int priority) {
			
			SecurityRule rule = new SecurityRule();
			rule.setDirection(direction);
			rule.setAccess(access);
			rule.setProtocol(protocol.toString());
			rule.setSourceAddressPrefix(sourceAddressPrefix);
			rule.setSourcePortRange(sourcePortRange);
			rule.setDestinationAddressPrefix(destinationAddressPrefix);
			rule.setDestinationPortRange(destinationPortRange);
			rule.setName(name);
			rule.setPriority(priority);
			return rule;
		}
		

		@Override
		public NetworkSecurityGroupImpl withAllowInbound(
			Protocol protocol, 
			String sourceAddressPrefix,
			String sourcePortRange, 
			String destinationAddressPrefix, 
			String destinationPortRange, 
			String name,
			int priority) {
			
			this.inner().getSecurityRules().add(
				createSecurityRule(
					SecurityRuleDirection.INBOUND, 
					SecurityRuleAccess.ALLOW,
					protocol, 
					sourceAddressPrefix, 
					sourcePortRange, 
					destinationAddressPrefix, 
					destinationPortRange, 
					name, 
					priority));
			return this;
		}


		@Override
		public NetworkSecurityGroupImpl withAllowOutbound(
			Protocol protocol, 
			String sourceAddressPrefix,
			String sourcePortRange, 
			String destinationAddressPrefix, 
			String destinationPortRange, 
			String name,
			int priority) {
			this.inner().getSecurityRules().add(
					createSecurityRule(
						SecurityRuleDirection.OUTBOUND, 
						SecurityRuleAccess.ALLOW,
						protocol, 
						sourceAddressPrefix, 
						sourcePortRange, 
						destinationAddressPrefix, 
						destinationPortRange, 
						name, 
						priority));
			return this;
		}


		@Override
		public NetworkSecurityGroupImpl withDenyInbound(
			Protocol protocol, 
			String sourceAddressPrefix,
			String sourcePortRange, 
			String destinationAddressPrefix, 
			String destinationPortRange, 
			String name,
			int priority) {

			this.inner().getSecurityRules().add(
				createSecurityRule(
					SecurityRuleDirection.INBOUND, 
					SecurityRuleAccess.DENY,
					protocol, 
					sourceAddressPrefix, 
					sourcePortRange, 
					destinationAddressPrefix, 
					destinationPortRange, 
					name, 
					priority));
			return this;
		}


		@Override
		public NetworkSecurityGroupImpl withDenyOutbound(
			Protocol protocol, 
			String sourceAddressPrefix,
			String sourcePortRange, 
			String destinationAddressPrefix, 
			String destinationPortRange, 
			String name,
			int priority) {
			
			this.inner().getSecurityRules().add(
				createSecurityRule(
					SecurityRuleDirection.OUTBOUND, 
					SecurityRuleAccess.DENY,
					protocol, 
					sourceAddressPrefix, 
					sourcePortRange, 
					destinationAddressPrefix, 
					destinationPortRange, 
					name, 
					priority));
			
			return this;
		}
	}
}

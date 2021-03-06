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

import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementService;
import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.NetworkResourceProviderService;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementService;
import com.microsoft.azure.shortcuts.common.implementation.Utils;
import com.microsoft.azure.shortcuts.resources.AvailabilitySet;
import com.microsoft.azure.shortcuts.resources.AvailabilitySets;
import com.microsoft.azure.shortcuts.resources.ResourceGroup;
import com.microsoft.azure.shortcuts.resources.ResourceGroups;
import com.microsoft.azure.shortcuts.resources.LoadBalancer;
import com.microsoft.azure.shortcuts.resources.LoadBalancers;
import com.microsoft.azure.shortcuts.resources.Network;
import com.microsoft.azure.shortcuts.resources.NetworkInterface;
import com.microsoft.azure.shortcuts.resources.NetworkInterfaces;
import com.microsoft.azure.shortcuts.resources.NetworkSecurityGroup;
import com.microsoft.azure.shortcuts.resources.NetworkSecurityGroups;
import com.microsoft.azure.shortcuts.resources.Networks;
import com.microsoft.azure.shortcuts.resources.Provider;
import com.microsoft.azure.shortcuts.resources.Providers;
import com.microsoft.azure.shortcuts.resources.PublicIpAddress;
import com.microsoft.azure.shortcuts.resources.PublicIpAddresses;
import com.microsoft.azure.shortcuts.resources.Publisher;
import com.microsoft.azure.shortcuts.resources.Publishers;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.Resource;
import com.microsoft.azure.shortcuts.resources.Resources;
import com.microsoft.azure.shortcuts.resources.Sizes;
import com.microsoft.azure.shortcuts.resources.StorageAccount;
import com.microsoft.azure.shortcuts.resources.StorageAccounts;
import com.microsoft.azure.shortcuts.resources.VirtualMachine;
import com.microsoft.azure.shortcuts.resources.VirtualMachines;
import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.configuration.PublishSettingsLoader;

import java.io.File;
import java.net.URI;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Subscription {
    public static String MANAGEMENT_URI = "https://management.core.windows.net/";
    public static String ARM_URL = "https://management.azure.com/";
    public static String ARM_AAD_URL = "https://login.windows.net/";

    private Configuration configuration;
    private ResourceManagementClient resourceManagementClient;
    private StorageManagementClient storageManagementClient;
    private ComputeManagementClient computeManagementClient;
    private NetworkResourceProviderClient networkResourceProviderClient;

    private final ResourcesImpl resources;
    private final ResourceGroupsImpl resourceGroups;
    private final ProvidersImpl providers;
    private final SizesImpl sizes;
    private final NetworksImpl networks;
    private final VirtualMachinesImpl virtualMachines;
    private final StorageAccountsImpl storageAccounts;
    private final AvailabilitySetsImpl availabilitySets;
    private final NetworkInterfacesImpl networkInterfaces;
    private final PublicIpAddressesImpl publicIpAddresses;
    private final LoadBalancersImpl loadBalancers;
    private final PublishersImpl publishers;
    private final NetworkSecurityGroupsImpl networkSecurityGroups;

    public static Subscription authenticate(String subscriptionId, String tenantId, String clientId, String clientKey) throws Exception {
    	return new Subscription(subscriptionId, tenantId, clientId, clientKey);
    }
    
    public static Subscription authenticate(String authenticationFilePath) throws Exception {
    	return authenticate(authenticationFilePath, null);
    }
    
    public static Subscription authenticate(String authenticationFilePath, String subscriptionId) throws Exception {
    	return new Subscription(authenticationFilePath, subscriptionId);
    }
    
    private Subscription(String subscriptionId, String tenantId, String clientId, String clientKey) throws Exception {
    	this(createConfiguration(subscriptionId, tenantId, clientId, clientKey, null, null, null));
    }

    
    private Subscription(String authenticationFilePath, String subscriptionId) throws Exception {
    	this(getConfigFromFile(authenticationFilePath, subscriptionId));
    }
    
    
    private Subscription(Configuration configuration) {
    	this.configuration = configuration;
        this.resources = new ResourcesImpl(this);
        this.resourceGroups = new ResourceGroupsImpl(this);
        this.providers = new ProvidersImpl(this);
        this.sizes = new SizesImpl(this);
        this.networks = new NetworksImpl(this);
        this.virtualMachines = new VirtualMachinesImpl(this);
        this.storageAccounts = new StorageAccountsImpl(this);
        this.availabilitySets = new AvailabilitySetsImpl(this);
        this.networkInterfaces = new NetworkInterfacesImpl(this);
        this.publicIpAddresses = new PublicIpAddressesImpl(this);
        this.loadBalancers = new LoadBalancersImpl(this);
        this.publishers = new PublishersImpl(this);
        this.networkSecurityGroups = new NetworkSecurityGroupsImpl(this);
    }
    
    
    // Returns an appropriate authenticated configuration based on the supplied file, automatically detecting if the file is a
    // PublishSettings file or an Azure shortcuts authentication file
    private static Configuration getConfigFromFile(String authFilePath, String subscriptionId) throws Exception {
    	Configuration config = getConfigurationFromAuthXml(new File(authFilePath), subscriptionId);
    	if(config != null) {
    		return config;
    	} else {
    		return PublishSettingsLoader.createManagementConfiguration(authFilePath, subscriptionId);
    	}
    }
    
    
    //**********************************************************
    //* Getters
    //**********************************************************

    public NetworkSecurityGroups networkSecurityGroups() {
    	return this.networkSecurityGroups;
    }
    
    public NetworkSecurityGroup networkSecurityGroups(String id) throws Exception {
    	return this.networkSecurityGroups().get(id);
    }
    
    public NetworkSecurityGroup networkSecurityGroups(String groupName, String name) throws Exception {
    	return this.networkSecurityGroups().get(groupName, name);
    }
    
    public Publishers publishers() {
    	return this.publishers;
    }
    
    public Publisher publishers(String id) throws Exception {
    	return this.publishers().get(id);
    }
    
    public Publisher publishers(Region region, String name) throws Exception {
    	return this.publishers().get(region, name);
    }
    
    public LoadBalancers loadBalancers() {
    	return this.loadBalancers;
    }
    
    public LoadBalancer loadBalancers(String id) throws Exception {
    	return this.loadBalancers().get(id);
    }
    
    public LoadBalancer loadBalancers(String groupName, String name) throws Exception {
    	return this.loadBalancers().get(groupName, name);
    }
    
    public AvailabilitySets availabilitySets() {
    	return this.availabilitySets;
    }
    
    public AvailabilitySet availabilitySets(String id) throws Exception {
    	return this.availabilitySets().get(id);
    }
    
    public AvailabilitySet availabilitySets(String groupName, String name) throws Exception {
    	return this.availabilitySets.get(groupName, name);
    }
    
    public NetworkInterfaces networkInterfaces() {
    	return this.networkInterfaces;
    }
    
    public NetworkInterface networkInterfaces(String id) throws Exception {
    	return this.networkInterfaces.get(id);
    }
    
    public NetworkInterface networkInterfaces(String groupName, String name) throws Exception {
    	return this.networkInterfaces.get(groupName, name);
    }
    
    public PublicIpAddresses publicIpAddresses() {
    	return this.publicIpAddresses;
    }
    
    public PublicIpAddress publicIpAddresses(String id) throws Exception {
    	return this.publicIpAddresses().get(id);
    }
    
    public PublicIpAddress publicIpAddresses(String groupName, String name) throws Exception {
    	return this.publicIpAddresses().get(groupName, name);
    }
    
    public ResourceGroups resourceGroups() {
    	return this.resourceGroups;
    }
    
    public ResourceGroup resourceGroups(String name) throws Exception {
    	return this.resourceGroups().get(name);
    }
    
    public Providers providers() {
    	return this.providers;
    }

    public Provider providers(String name) throws Exception {
    	return this.providers().get(name);
    }
    
    public Resources resources() {
    	return this.resources;
    }
    
    public Resource resources(String id) throws Exception {
    	return this.resources().get(id);
    }
    
    public Sizes sizes() {
    	return this.sizes;
    }

    public Networks networks() {
    	return this.networks;
    }
    
    public Network networks(String id) throws Exception {
    	return this.networks().get(id);
    }
    
    public Network networks(String group, String name) throws Exception {
    	return this.networks().get(group, name);
    }
    
    public VirtualMachines virtualMachines() {
    	return this.virtualMachines;
    }

    public VirtualMachine virtualMachines(String id) throws Exception {
    	return this.virtualMachines().get(id);
    }
    
    public VirtualMachine virtualMachines(String group, String name) throws Exception {
    	return this.virtualMachines().get(group, name);
    }
    
    public StorageAccounts storageAccounts() {
    	return this.storageAccounts;
    }
    
    public StorageAccount storageAccounts(String id) throws Exception {
    	return this.storageAccounts().get(id);
    }
    
    public StorageAccount storageAccounts(String group, String name) throws Exception {
    	return this.storageAccounts().get(group, name);
    }
    
    // Returns an ARM authenticated configuration based on the provided Azure shortcuts authentication file
    // The assumed schema of the file is:
    // <azureShortcutsAuth>
    //   <subscription 
    //		id="<subscription-id>" 
    //		tenant="<tenant-id>"
    //		client="<client-id>"
    //		key="<client-key>"
    //		managementURI="<management-URI>"
    //		baseURL="<base-ARM-URL>"
    //		authURL="<active-directory-login-url>"
    //	 />
    // </azureShortcutsAuth>
    private static Configuration getConfigurationFromAuthXml(File authFile, String subscriptionId) throws Exception {
    	Document xmlDoc = Utils.loadXml(authFile);
    	Element root = xmlDoc.getDocumentElement();    	
    	if(!root.getTagName().equals("azureShortcutsAuth")) {
    		return null;
    	} 
    	
    	NodeList subscriptions = root.getElementsByTagName("subscription");
    	if(subscriptions.getLength() == 0) {
    		throw new ParserConfigurationException("No subscriptions found");
    	}

    	Element subscription = null;
    	if(subscriptionId == null) {
    		// If no specific subscription ID requested, assume the first one
    		subscription = (Element)subscriptions.item(0);
    	} else {
    		// Else, find the subscription with the requested ID
    		for(int i=0; i<subscriptions.getLength(); i++) {
    			subscription = (Element)subscriptions.item(i);
    			if(subscription.getAttribute("id").equals(subscriptionId)) {
    				break;
    			} else {
    				subscription = null;
    			}
    		}
    	}
    	
		if(subscription == null) {
			throw new ParserConfigurationException("Subscription not found");
		}
		
		// Extract service principal information
		subscriptionId = subscription.getAttribute("id");
		String tenantId = subscription.getAttribute("tenant");
		String clientId = subscription.getAttribute("client");
		String clientKey = subscription.getAttribute("key");
		String managementUri = subscription.getAttribute("managementURI");
		String baseUrl = subscription.getAttribute("baseURL");
		String authUrl = subscription.getAttribute("authURL");
		return createConfiguration(subscriptionId, tenantId, clientId, clientKey, managementUri, baseUrl, authUrl);
    }
    
    
    // Returns the compute management client, creating if needed
    ComputeManagementClient computeManagementClient() {
    	if(this.computeManagementClient == null) {
    		this.computeManagementClient = ComputeManagementService.create(this.configuration);
    	}
    	
    	return this.computeManagementClient;
    }
    
    
    // Returns the network management client, creating if needed
    NetworkResourceProviderClient networkManagementClient() {
    	if(this.networkResourceProviderClient == null) {
    		this.networkResourceProviderClient = NetworkResourceProviderService.create(this.configuration);
    	}
    	
    	return this.networkResourceProviderClient;
    }
    
    
    // Returns the resource management client, creating if needed
    ResourceManagementClient resourceManagementClient() {
    	if(this.resourceManagementClient == null) {
    		this.resourceManagementClient = ResourceManagementService.create(this.configuration);
    	}
    	
    	return this.resourceManagementClient;

    }

    
    // Returns the storage management client
    StorageManagementClient storageManagementClient() {
    	if(this.storageManagementClient == null) {
    		this.storageManagementClient = StorageManagementService.create(this.configuration);
    	}
    	
    	return this.storageManagementClient;
    }
    
    
	private static Configuration createConfiguration(
			String subscriptionId, 
			String tenantId, 
			String clientId, 
			String clientKey,
			String managementUri,
			String baseUrl,
			String authUrl) throws Exception {
		
		if(subscriptionId == null) {
			throw new Exception("Missing subscription");
		}
		
		if(baseUrl == null) {
			baseUrl = Subscription.ARM_URL;
		}
		URI baseUri = new URI(baseUrl);
		
		if(managementUri == null) {
			managementUri = Subscription.MANAGEMENT_URI;
		}
		
		if(authUrl == null) {
			authUrl = Subscription.ARM_AAD_URL;
		}
		
		String accessToken = AuthHelper.getAccessTokenFromServicePrincipalCredentials(
			managementUri, 
			authUrl,
			tenantId, 
			clientId, 
			clientKey).getAccessToken();
		
		return ManagementConfiguration.configure(
			(String)null, 
			baseUri,
			subscriptionId,
			accessToken);
	}

}

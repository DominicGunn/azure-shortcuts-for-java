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

package com.microsoft.azure.shortcuts.resources.samples;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.shortcuts.resources.PublicIpAddress;
import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.implementation.Subscription;

public class PublicIpAddressesSample {
    public static void main(String[] args) {
        try {
            Subscription subscription = Subscription.authenticate("my.azureauth", null);
            test(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Subscription subscription) throws Exception {
    	String existingGroupName = "javasampleresourcegroup1";
    	String newPublicIpAddressName = "testPIP";
    	
    	// Listing all public IP addresses
    	Map<String, PublicIpAddress> pips = subscription.publicIpAddresses().asMap();
    	System.out.println("Public IP addresses:");
    	for(PublicIpAddress pip : pips.values()) {
    		printPIP(pip);
    	}
    	
    	// Create a public IP address in a default new group
    	PublicIpAddress pipMinimal = subscription.publicIpAddresses().define(newPublicIpAddressName)
    		.withRegion(Region.US_WEST)
    		.withNewResourceGroup()
    		.provision();
    	
    	// Get info about a specific PIP using its group and name
    	pipMinimal = subscription.publicIpAddresses(pipMinimal.id());
    	pipMinimal = subscription.publicIpAddresses().get(pipMinimal.id());
    	String groupNameCreated = pipMinimal.resourceGroup(); 
    	printPIP(pipMinimal);

    	// More detailed PIP definition
    	PublicIpAddress pip = subscription.publicIpAddresses().define(newPublicIpAddressName + "2")
    		.withRegion(Region.US_WEST)
    		.withExistingResourceGroup(existingGroupName)
    		.withLeafDomainLabel("hellomarcins")
    		.withStaticIp()
    		.withTag("hello", "world")
    		.provision();
    	    	
    	// Listing PIPs in a specific resource group
    	pips = subscription.publicIpAddresses().asMap(existingGroupName);
    	System.out.println(String.format("PIP ids in group '%s': \n\t%s", existingGroupName, StringUtils.join(pips.keySet(), ",\n\t")));
    	
    	// Get info about a specific PIP using its resource ID
    	pip = subscription.publicIpAddresses(pip.resourceGroup(), pip.name());
    	printPIP(pip);
    	
    	// Delete the PIP
    	pipMinimal.delete();
    	pip.delete();
    	
    	// Delete the auto-created group
    	subscription.resourceGroups(groupNameCreated).delete();
    }
    
    
    private static void printPIP(PublicIpAddress pip) throws Exception {
    	StringBuilder output = new StringBuilder();
    	output
    		.append(String.format("Public IP ID: %s\n", pip.id()))
    		.append(String.format("\tName: %s\n", pip.name()))
    		.append(String.format("\tGroup: %s\n", pip.resourceGroup()))
    		.append(String.format("\tRegion: %s\n", pip.region()))
    		.append(String.format("\tIP Address: %s\n", pip.ipAddress()))
    		.append(String.format("\tLeaf domain label: %s\n", pip.leafDomainLabel()))
    		.append(String.format("\tFQDN: %s\n", pip.inner().getDnsSettings().getFqdn()))
    		;
    	
    	System.out.println(output.toString());
    }
}

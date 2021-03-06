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

import org.apache.commons.lang.StringUtils;

import com.microsoft.azure.shortcuts.resources.Region;
import com.microsoft.azure.shortcuts.resources.Size;
import com.microsoft.azure.shortcuts.resources.VirtualMachine;
import com.microsoft.azure.shortcuts.resources.implementation.Subscription;

// Tests resources
public class VirtualMachinesSample {
    public static void main(String[] args) {
        try {
            Subscription subscription = Subscription.authenticate("my.azureauth");
            test(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void test(Subscription subscription) throws Exception {
    	// Creating a Windows VM
    	String deploymentId = String.valueOf(System.currentTimeMillis());
    	String groupName = "group" + deploymentId;

    	VirtualMachine vmWin = subscription.virtualMachines().define("vm" + deploymentId)
    		.withRegion(Region.US_WEST)
    		.withNewResourceGroup(groupName)
    		.withNewNetwork("net" + deploymentId, "10.0.0.0/28")
    		.withPrivateIpAddressDynamic()
    		.withNewPublicIpAddress()
    		.withAdminUsername("shortcuts")
    		.withAdminPassword("Abcd.1234")
    		.withLatestImage("MicrosoftWindowsServer", "WindowsServer", "2008-R2-SP1")
    		.withSize(Size.Type.BASIC_A1)
    		.withNewStorageAccount()
    		.withNewDataDisk(100)
    		//.withExistingDataDisk("https://vm1455045717874store.blob.core.windows.net/vm1455045717874/disk0.vhd")
    		.provision();
    
    	printVM(vmWin);
    	
    	// Listing all virtual machine ids in a subscription
    	Map<String, VirtualMachine> vms = subscription.virtualMachines().asMap();
    	System.out.println(String.format("Virtual machines: \n\t%s",  StringUtils.join(vms.keySet(), "\n\t")));

    	// Adding a Linux VM to the same group and VNet
    	VirtualMachine vmLinux = subscription.virtualMachines().define("lx" + deploymentId)
    		.withRegion(Region.US_WEST)
    		.withExistingResourceGroup(groupName)
    		.withExistingNetwork(subscription.networks(groupName, "net" + deploymentId))
    		.withSubnet("subnet1")
    		.withPrivateIpAddressDynamic()
    		.withNewPublicIpAddress()
    		.withAdminUsername("shortcuts")
    		.withAdminPassword("Abcd.1234")
    		.withLatestImage("Canonical", "UbuntuServer", "14.04.3-LTS")
    		.withSize(Size.Type.BASIC_A1)
    		.provision();
    	    	
    	// Listing vms in a specific group
    	Map<String, VirtualMachine> vmsInGroup = subscription.virtualMachines().asMap(groupName);
    	System.out.println(String.format("Virtual machines: \n\t%s", StringUtils.join(vmsInGroup.keySet(), "\n\t")));
    	
    	// Listing virtual machines as objects
    	String vmID = null;
    	for(VirtualMachine vm : vms.values()) {
    		if(vmID == null) {
    			vmID = vm.id();
    		}
    		printVM(vm);
    	}
    	
    	// Getting a specific virtual machine using its id
    	VirtualMachine vm = subscription.virtualMachines(vmID);
    	printVM(vm);
    	
    	// Getting a specific virtual machine using its group and name
    	vm = subscription.virtualMachines(groupName, vm.computerName());
    	printVM(vm);
    	
    	// Restart the VM
    	vmWin.restart();
    	
    	// Deallocate the VM
    	vmWin.deallocate();
    	
    	// Delete VM
    	vmWin.delete();
    	
    	// Delete the group
    	subscription.resourceGroups().delete(groupName);
	}
    
    
    private static void printVM(VirtualMachine vm) {
		StringBuilder info = new StringBuilder();
		info
			.append(String.format("Information about vm: %s\n", vm.id()))
			.append(String.format("\tAdmin username: %s\n", vm.adminUserName()))
			.append(String.format("\tAvailability set URI: %s\n", vm.availabilitySet()))
			.append(String.format("\tBoot diagnostics storage URI: %s\n", vm.bootDiagnosticsStorage()))
			.append(String.format("\tComputer name: %s\n", vm.computerName()))
			.append(String.format("\tCustom data: %s\n", vm.customData()))
			.append(String.format("\tNumber of data disks: %d\n", vm.dataDisks().size()))
			.append(String.format("\tNumber of extensions: %d\n", (vm.extensions()!=null) ? vm.extensions().size() : 0))
			.append(String.format("\tGroup: %s\n", vm.resourceGroup()))
			;
			
		System.out.println(info.toString());
    }
 }

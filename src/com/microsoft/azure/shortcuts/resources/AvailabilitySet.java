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
package com.microsoft.azure.shortcuts.resources;

import java.util.List;

import com.microsoft.azure.shortcuts.common.Deletable;
import com.microsoft.azure.shortcuts.common.Provisionable;
import com.microsoft.azure.shortcuts.common.Refreshable;
import com.microsoft.azure.shortcuts.common.Wrapper;
import com.microsoft.azure.shortcuts.resources.common.GroupResourceBase;

public interface AvailabilitySet extends 
	GroupResourceBase,
	Refreshable<AvailabilitySet>,
	Wrapper<com.microsoft.azure.management.compute.models.AvailabilitySet>,
	Deletable {
	
	/**
	 * @return IDs of virtual machines in this availability set
	 */
	List<String> virtualMachineIds();
	
	public interface DefinitionBlank extends 
		GroupResourceBase.DefinitionWithRegion<DefinitionWithGroup> {
	}
	
	public interface DefinitionWithGroup extends 
		GroupResourceBase.DefinitionWithResourceGroup<DefinitionProvisionable> {}
	
	public interface DefinitionProvisionable extends 
		GroupResourceBase.DefinitionWithResourceGroup<DefinitionProvisionable>,
		GroupResourceBase.DefinitionWithTags<DefinitionProvisionable>,
		Provisionable<AvailabilitySet> {	
	}
}

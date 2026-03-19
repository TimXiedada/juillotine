/* SPDX-License-Identifier: Apache-2.0 */
/*
   Copyright (c) 2026 Xie Youtian. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package net.xiedada.juillotine.adapters;

import net.xiedada.juillotine.Service;

public interface IAdapter {
    // Create a shortcode
    public String add(String URL, String shortcode, Service.Options options);

    // Standard query and reverse query
    public String find(String shortcode);

    public String codeFor(String URL);

    // Remove an entry
    public void clear(String shortcode);

    public void clearCode(String URL);
}

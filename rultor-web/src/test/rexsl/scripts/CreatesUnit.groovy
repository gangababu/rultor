/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.web.rexsl.scripts

import com.jcabi.manifests.Manifests
import com.jcabi.urn.URN
import com.rexsl.page.auth.Identity
import com.rultor.client.RestUser
import com.rultor.spi.Spec
import com.rultor.web.AuthKeys
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

Manifests.append(new File(rexsl.basedir, 'target/test-classes/META-INF/MANIFEST.MF'))
def identity = new Identity.Simple(new URN('urn:test:1'), '', new URI('#'))
def key = new AuthKeys().make(identity)
def user = new RestUser(rexsl.home, identity.urn(), key)
MatcherAssert.assertThat(user.urn(), Matchers.equalTo(identity.urn()))

def name = 'sample-unit'
def unit = user.units().get(name)
if (unit == null) {
    unit = user.create(name)
}

[
    'java.lang.Double ( -55.0 )': 'java.lang.Double(-55.0)',
    'java.lang.String: \r\ns \n\n\t\r\u20ac t': 'java.lang.String:\ns \n\n\t\r\u20ac t',
].each {
    unit.spec(new Spec.Simple(it.key))
    MatcherAssert.assertThat(unit.spec().asText(), Matchers.equalTo(it.value))
}
user.remove(name)
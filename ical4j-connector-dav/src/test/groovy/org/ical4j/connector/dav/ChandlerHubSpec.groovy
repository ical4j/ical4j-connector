/**
 * Copyright (c) 2012, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ical4j.connector.dav


import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class ChandlerHubSpec extends Specification {
	
	String username = ''
	String password = ''
	
	@Shared def calendarStore
	
	def setupSpec() {
		def prodId = '-//Ben Fortuna//iCal4j Connector 1.0//EN'
		def url = new URL('https://hub.chandlerproject.org')
		def pathResolver = PathResolver.Defaults.CHANDLER;
		
		calendarStore = new CalDavCalendarStore(prodId, url, pathResolver)
	}
	
	def cleanupSpec() {
		calendarStore.disconnect()
	}
	
	@Ignore
	def 'verify successful connection'() {
		setup: 'connect to chandler hub'
		assert calendarStore.connect(new DavSessionConfiguration().withUser(username).withPassword( password.toCharArray()))
		
		expect: 'is connected'
		assert calendarStore.isConnected()
	}
	
	@Ignore
	def 'retrieve collections from store'() {
		expect:
		calendarStore.getCollections().each {
			println it.displayName
		}
	}
}
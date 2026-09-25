package org.ical4j.connector.msgraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*
 * Copyright (c) 2026, Ben Fortuna
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

/**
 * Resolves Microsoft Graph time zone names to {@link ZoneId}. Graph returns event time zones as
 * Windows zone names (e.g. {@code "AUS Eastern Standard Time"}) by default, which {@link ZoneId#of}
 * cannot parse; this helper translates them to IANA ids using a bundled CLDR-derived mapping.
 *
 * <p>A name that is already a valid IANA id (e.g. {@code "Australia/Melbourne"}, {@code "UTC"}) is
 * used directly. Unrecognised names fall back to {@link ZoneOffset#UTC} with a logged warning.
 */
final class TimeZoneMapping {

    private static final Logger LOG = LoggerFactory.getLogger(TimeZoneMapping.class);

    private static final Map<String, String> WINDOWS_TO_IANA = load();

    private TimeZoneMapping() {
    }

    private static Map<String, String> load() {
        Properties props = new Properties();
        try (InputStream in = TimeZoneMapping.class.getResourceAsStream("windows-iana.properties")) {
            if (in == null) {
                LOG.warn("windows-iana.properties not found; Windows time zone names will fall back to UTC");
                return Collections.emptyMap();
            }
            props.load(in);
        } catch (IOException e) {
            LOG.warn("Failed to load windows-iana.properties; Windows time zone names will fall back to UTC", e);
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        for (String name : props.stringPropertyNames()) {
            map.put(name, props.getProperty(name));
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * @param graphTimeZone a Microsoft Graph time zone name (IANA or Windows), may be null
     * @return the corresponding {@link ZoneId}, or UTC when the name is null, blank, or unrecognised
     */
    static ZoneId toZoneId(String graphTimeZone) {
        if (graphTimeZone == null || graphTimeZone.isBlank()) {
            return ZoneOffset.UTC;
        }
        // Already an IANA id (or a fixed-offset id) that java.time understands.
        try {
            return ZoneId.of(graphTimeZone);
        } catch (RuntimeException ignored) {
            // fall through to Windows-name lookup
        }
        String iana = WINDOWS_TO_IANA.get(graphTimeZone);
        if (iana != null) {
            try {
                return ZoneId.of(iana);
            } catch (RuntimeException e) {
                LOG.warn("Mapped IANA zone '{}' for Windows zone '{}' is not resolvable; defaulting to UTC",
                        iana, graphTimeZone);
                return ZoneOffset.UTC;
            }
        }
        LOG.warn("Unrecognised time zone name '{}'; defaulting to UTC", graphTimeZone);
        return ZoneOffset.UTC;
    }
}

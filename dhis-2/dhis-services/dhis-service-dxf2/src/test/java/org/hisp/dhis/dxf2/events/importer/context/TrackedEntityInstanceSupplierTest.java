/*
 * Copyright (c) 2004-2021, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.dxf2.events.importer.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.security.acl.AclService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;

/**
 * @author Luciano Fiandesio
 */
@MockitoSettings( strictness = Strictness.LENIENT )
class TrackedEntityInstanceSupplierTest
    extends AbstractSupplierTest<TrackedEntityInstance, Set<TrackedEntityInstance>>

{

    private TrackedEntityInstanceSupplier subject;

    @Mock
    private AclService aclService;

    @Mock
    private Environment environment;

    @BeforeEach
    void setUp()
    {
        this.subject = new TrackedEntityInstanceSupplier( jdbcTemplate, environment );
        when( environment.getActiveProfiles() ).thenReturn( new String[] {} );
    }

    @Test
    void handleNullEvents()
    {
        assertNotNull( subject.get( null ) );
    }

    @Test
    void verifySupplier()
        throws SQLException
    {
        // mock resultset data
        when( mockResultSet.getLong( "trackedentityinstanceid" ) ).thenReturn( 100L );
        when( mockResultSet.getString( "uid" ) ).thenReturn( "abcded" );
        when( mockResultSet.getString( "code" ) ).thenReturn( "ALFA" );
        // create event to import
        Event event = new Event();
        event.setUid( CodeGenerator.generateUid() );
        event.setTrackedEntityInstance( "abcded" );
        // mock resultset extraction
        mockResultSetExtractor( mockResultSet );

        Set<TrackedEntityInstance> map = subject.get(
            new HashSet<>( Arrays.asList( event.getUid() ) ) );

        TrackedEntityInstance trackedEntityInstance = map.iterator().next();

        assertThat( trackedEntityInstance, is( notNullValue() ) );
        assertThat( trackedEntityInstance.getId(), is( 100L ) );
        assertThat( trackedEntityInstance.getUid(), is( "abcded" ) );
        assertThat( trackedEntityInstance.getCode(), is( "ALFA" ) );
    }
}

/*
 * Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.dataexchange.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.common.IdScheme;
import org.hisp.dhis.dataexchange.analytics.AnalyticsDataExchange;
import org.hisp.dhis.dataexchange.analytics.AnalyticsDataExchangeStore;
import org.hisp.dhis.dataexchange.analytics.Api;
import org.hisp.dhis.dataexchange.analytics.Filter;
import org.hisp.dhis.dataexchange.analytics.Source;
import org.hisp.dhis.dataexchange.analytics.SourceRequest;
import org.hisp.dhis.dataexchange.analytics.Target;
import org.hisp.dhis.dataexchange.analytics.TargetRequest;
import org.hisp.dhis.dataexchange.analytics.TargetType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AnalyticsDataExchangeStoreTest
    extends DhisSpringTest
{
    @Autowired
    private AnalyticsDataExchangeStore store;

    @Test
    void testSaveGet()
    {
        AnalyticsDataExchange deA = getAnayticsDataExchange( 'A' );
        AnalyticsDataExchange deB = getAnayticsDataExchange( 'B' );

        store.save( deA );
        store.save( deB );

        assertNotNull( store.getByUid( deA.getUid() ) );
        assertNotNull( store.getByUid( deB.getUid() ) );
    }

    @Test
    void testUpdate()
    {
        AnalyticsDataExchange de = getAnayticsDataExchange( 'A' );

        store.save( de );

        assertNotNull( de.getSource().getRequests().get( 0 ) );

        de.getSource().getRequests().get( 0 ).getDx().add( "NhSFzklRD55" );

        store.update( de );

        assertEquals( 3, de.getSource().getRequests().get( 0 ).getDx().size() );
    }

    private AnalyticsDataExchange getAnayticsDataExchange( char uniqueChar )
    {
        SourceRequest sourceRequest = new SourceRequest();
        sourceRequest.setDx( List.of( "LrDpG50RAU9", "uR5HCiJhQ1w" ) );
        sourceRequest.setPe( List.of( "202201", "202202" ) );
        sourceRequest.setOu( List.of( "G9BuXqtNeeb", "jDgiLmYwPDm" ) );
        sourceRequest.setFilters( List.of(
            new Filter( "MuTwGW0BI4o", List.of( "v9oULMMdmzE", "eJHJ0bfDCEO" ) ),
            new Filter( "dAOgE7mgysJ", List.of( "rbE2mZX86AA", "XjOFfrPwake" ) ) ) );

        Source source = new Source();
        source.setRequests( List.of( sourceRequest ) );

        Target target = new Target();
        target.setApi( new Api( "https://play.dhis2.org/demo", "jk6NhU4GF8I" ) );
        target.setType( TargetType.EXTERNAL );
        target.setRequest( new TargetRequest( IdScheme.UID, IdScheme.UID, IdScheme.UID, IdScheme.UID ) );

        AnalyticsDataExchange exchange = new AnalyticsDataExchange();
        exchange.setAutoFields();
        exchange.setName( "DataExchange" + uniqueChar );
        exchange.setSource( source );
        exchange.setTarget( target );
        return exchange;
    }
}

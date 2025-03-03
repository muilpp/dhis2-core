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
package org.hisp.dhis.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.hisp.dhis.IntegrationTestBase;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitStore;
import org.hisp.dhis.query.operators.EqualOperator;
import org.hisp.dhis.query.operators.NullOperator;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
class QueryParserTest extends IntegrationTestBase
{

    private QueryParser queryParser;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private OrganisationUnitStore organisationUnitStore;

    @Override
    public boolean emptyDatabaseAfterTest()
    {
        return true;
    }

    @Autowired
    private UserService _userService;

    @Override
    protected void setUpTest()
        throws Exception
    {
        this.userService = _userService;

        OrganisationUnit orgUnitA = createOrganisationUnit( 'A' );
        organisationUnitStore.save( orgUnitA );

        User user = createAndAddUser( "A" );
        user.addOrganisationUnit( orgUnitA );
        userService.updateUser( user );

        injectSecurityContext( user );

        queryParser = new DefaultJpaQueryParser( schemaService );
    }

    @Test
    void failedFilters()
    {
        assertThrows( QueryParserException.class,
            () -> queryParser.parse( DataElement.class, Arrays.asList( "id", "name" ) ) );
    }

    @Test
    void eqOperator()
        throws QueryParserException
    {
        Query query = queryParser.parse( DataElement.class, Arrays.asList( "id:eq:1", "id:eq:2" ) );
        assertEquals( 2, query.getCriterions().size() );
        Restriction restriction = (Restriction) query.getCriterions().get( 0 );
        assertEquals( "id", restriction.getPath() );
        assertEquals( "1", restriction.getOperator().getArgs().get( 0 ) );
        assertTrue( restriction.getOperator() instanceof EqualOperator );
        restriction = (Restriction) query.getCriterions().get( 1 );
        assertEquals( "id", restriction.getPath() );
        assertEquals( "2", restriction.getOperator().getArgs().get( 0 ) );
        assertTrue( restriction.getOperator() instanceof EqualOperator );
    }

    @Test
    void eqOperatorDeepPath1()
        throws QueryParserException
    {
        Query query = queryParser.parse( DataElement.class,
            Arrays.asList( "dataElementGroups.id:eq:1", "dataElementGroups.id:eq:2" ) );
        assertEquals( 2, query.getCriterions().size() );
        Restriction restriction = (Restriction) query.getCriterions().get( 0 );
        assertEquals( "dataElementGroups.id", restriction.getPath() );
        assertEquals( "1", restriction.getOperator().getArgs().get( 0 ) );
        assertTrue( restriction.getOperator() instanceof EqualOperator );
        restriction = (Restriction) query.getCriterions().get( 1 );
        assertEquals( "dataElementGroups.id", restriction.getPath() );
        assertEquals( "2", restriction.getOperator().getArgs().get( 0 ) );
        assertTrue( restriction.getOperator() instanceof EqualOperator );
    }

    @Test
    void eqOperatorDeepPathFail()
    {
        assertThrows( QueryParserException.class, () -> queryParser.parse( DataElement.class,
            Arrays.asList( "dataElementGroups.id.name:eq:1", "dataElementGroups.id.abc:eq:2" ) ) );
    }

    @Test
    void nullOperator()
        throws QueryParserException
    {
        Query query = queryParser.parse( DataElement.class, Arrays.asList( "id:null", "name:null" ) );
        assertEquals( 2, query.getCriterions().size() );
        Restriction restriction = (Restriction) query.getCriterions().get( 0 );
        assertEquals( "id", restriction.getPath() );
        assertTrue( restriction.getOperator() instanceof NullOperator );
        restriction = (Restriction) query.getCriterions().get( 1 );
        assertEquals( "name", restriction.getPath() );
        assertTrue( restriction.getOperator() instanceof NullOperator );
    }
}

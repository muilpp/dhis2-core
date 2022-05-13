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
package org.hisp.dhis.dataexchange.analytics.service;

import static org.hisp.dhis.common.DimensionalObject.DATA_X_DIM_ID;
import static org.hisp.dhis.common.DimensionalObject.ORGUNIT_DIM_ID;
import static org.hisp.dhis.common.DimensionalObject.PERIOD_DIM_ID;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.hisp.dhis.analytics.AnalyticsService;
import org.hisp.dhis.analytics.DataQueryParams;
import org.hisp.dhis.analytics.DataQueryService;
import org.hisp.dhis.common.DimensionalObject;
import org.hisp.dhis.common.IdScheme;
import org.hisp.dhis.dataexchange.analytics.model.AnalyticsDataExchange;
import org.hisp.dhis.dataexchange.analytics.model.Filter;
import org.hisp.dhis.dataexchange.analytics.model.SourceRequest;
import org.hisp.dhis.dataexchange.analytics.model.TargetRequest;
import org.hisp.dhis.dataexchange.analytics.model.TargetType;
import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.datavalueset.DataValueSet;
import org.hisp.dhis.dxf2.datavalueset.DataValueSetService;
import org.hisp.dhis.dxf2.importsummary.ImportSummaries;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsDataExchangeService
{
    private final AnalyticsService analyticsService;

    private final DataQueryService dataQueryService;

    private final DataValueSetService dataValueSetService;

    public ImportSummaries exchangeData( AnalyticsDataExchange exchange )
    {
        ImportSummaries summaries = new ImportSummaries();

        exchange.getSource().getRequests()
            .forEach( request -> summaries.addImportSummary( exchangeData( exchange, request ) ) );

        return summaries;
    }

    private ImportSummary exchangeData( AnalyticsDataExchange exchange, SourceRequest request )
    {
        DataValueSet dataValueSet = analyticsService.getAggregatedDataValueSet( toDataQueryParams( request ) );

        return exchange.getTarget().getType() == TargetType.INTERNAL ? pushToInternal( exchange, dataValueSet )
            : pushToExternal( exchange, dataValueSet );
    }

    private ImportSummary pushToInternal( AnalyticsDataExchange exchange, DataValueSet dataValueSet )
    {
        return dataValueSetService.importDataValueSet( dataValueSet, toImportOptions( exchange ) );
    }

    private ImportSummary pushToExternal( AnalyticsDataExchange exchange, DataValueSet dataValueSet )
    {
        return null; // TODO
    }

    private ImportOptions toImportOptions( AnalyticsDataExchange exchange )
    {
        TargetRequest request = exchange.getTarget().getRequest();

        return new ImportOptions()
            .setDataElementIdScheme( toNameOrDefault( request.getDataElementIdScheme() ) )
            .setOrgUnitIdScheme( toNameOrDefault( request.getOrgUnitIdScheme() ) )
            .setCategoryOptionComboIdScheme( toNameOrDefault( request.getCategoryOptionComboIdScheme() ) )
            .setIdScheme( toNameOrDefault( request.getIdScheme() ) );
    }

    private DataQueryParams toDataQueryParams( SourceRequest request )
    {
        IdScheme inputIdScheme = getOrDefault( request.getInputIdScheme() );

        List<DimensionalObject> filters = request.getFilters().stream()
            .map( f -> toDimensionalObject( f, inputIdScheme ) )
            .collect( Collectors.toList() );

        return DataQueryParams.newBuilder()
            .addDimension( toDimensionalObject( DATA_X_DIM_ID, request.getDx(), inputIdScheme ) )
            .addDimension( toDimensionalObject( PERIOD_DIM_ID, request.getPe(), inputIdScheme ) )
            .addDimension( toDimensionalObject( ORGUNIT_DIM_ID, request.getOu(), inputIdScheme ) )
            .addFilters( filters )
            .build();
    }

    private DimensionalObject toDimensionalObject( String dimension, List<String> items, IdScheme inputIdScheme )
    {
        return dataQueryService.getDimension(
            dimension, items, null, null, null, false, inputIdScheme );
    }

    private DimensionalObject toDimensionalObject( Filter filter, IdScheme inputIdScheme )
    {
        return dataQueryService.getDimension(
            filter.getDimension(), filter.getItems(), null, null, null, false, inputIdScheme );
    }

    /**
     * Returns a canonical name of the given ID scheme, or the name of the
     * default ID scheme if the given ID scheme is null.
     *
     * @param idScheme the {@link IdScheme}.
     * @return a canonical name.
     */
    public static String toNameOrDefault( IdScheme idScheme )
    {
        return idScheme != null ? idScheme.name() : IdScheme.UID.name();
    }

    /**
     * Returns the given ID scheme, or the default ID scheme if null.
     *
     * @param idScheme the {@link IdScheme}.
     * @return the given ID scheme, or the default ID scheme if null.
     */
    public IdScheme getOrDefault( IdScheme idScheme )
    {
        return idScheme != null ? idScheme : IdScheme.UID;
    }
}

<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
    <sch:ns uri="http://dimo-omp.de" prefix="dimo"/>
    <sch:pattern>
        <sch:rule context="dimo:GetBookingsFunction">
            <!--<sch:assert test="GetBookingsResponse[@id='34']">Id must be 34</sch:assert>-->
            <!--<sch:assert test="GetBookingsResponse/UserId = 2">UserId must be 2</sch:assert>-->
            <!--<sch:report test="GetBookingsRequest/Filter = UserId">A</sch:report>-->
            <!--<sch:report test="boolean(GetBookingsRequest/Equals)">B</sch:report>-->
            <!--<sch:report test="GetBookingsResponse/UserId = GetBookingsRequest/Equals">C</sch:report>-->
            <!--<sch:assert test="GetBookingsRequest/Filter = 'UserId'">A</sch:assert>-->
            <!--<sch:assert test="boolean(GetBookingsRequest/Equals)">B</sch:assert>-->
            <!--<sch:assert test="GetBookingsResponse/UserId = GetBookingsRequest/Equals">C</sch:assert>-->
            <sch:assert test="not(dimo:GetBookingsRequest/dimo:Filter = 'UserId' and boolean(dimo:GetBookingsRequest/dimo:Equals)) or dimo:GetBookingsResponse/dimo:UserId = dimo:GetBookingsRequest/dimo:Equals">UserId must be 4</sch:assert>
        </sch:rule>
    </sch:pattern>
</sch:schema>

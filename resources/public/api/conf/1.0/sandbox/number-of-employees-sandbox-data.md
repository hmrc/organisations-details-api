<table>
    <col width="100%">
    <thead>
    <tr>
        <th>Valid payload</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>
            <p>{&quot;fromDate&quot;:&quot;2019-10-01&quot;,
                &quot;toDate&quot;:&quot;2020-04-05&quot;,
                &quot;payeReference&quot;:[
                    {
                    &quot;districtNumber&quot;:&quot;456&quot;,
                    &quot;schemeReference&quot;:&quot;RT882d&quot;
                    },
                    {
                    &quot;districtNumber&quot;:&quot;123&quot;,
                    &quot;schemeReference&quot;:&quot;AB888666&quot;
                    }
                ]}
            </p>
        </td>
    </tr>
    </tbody>
</table>

<table>
    <col width="25%">
    <col width="35%">
    <col width="40%">
    <thead>
    <tr>
        <th>Scenario</th>
        <th>Payload</th>
        <th>Response</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td><p>Successful match</p>
        <td><p>fromDate = &quot;123456789A&quot;
            <br/>employerName = &quot;Waitrose&quot;
            <br/>addressLine1 = &quot;123 Long Road&quot;
            <br/>addressLine2 = &quot;Some City&quot;
            <br/>addressLine3 = &quot;Some County&quot;
            <br/>addressLine4 = &quot;&quot;
            <br/>postcode = &quot;AB12 3CD&quot;</p></td>
        <td><p>200 (OK)</p><p>Payload as response example above</p></td>
    </tr>
    <tr>
        <td><p>No match</p></td>
        <td>
            <p>Any details that are not an exact match.</p>
        </td>
        <td><p>403 (Forbidden)</p>
        <p>{ &quot;code&quot; : &quot;MATCHING_FAILED&quot;,<br/>&quot;message&quot; : &quot;There is no match for the information provided&quot; }</p></td>
    </tr>
    <tr>
          <td>
            <p>Missing companyRegistrationNumber &#47; 
                    employerName &#47; 
                    addressLine1 &#47; 
                    addressLine2 &#47;
                    postcode
            </p>
          </td>
          <td><p>Any field missing</p></td>
          <td><p>400 (Bad Request)</p>
          <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;&#60;field_name&#62; is required&quot; }</p></td>
    </tr>
    <tr>
        <td><p>Malformed companyRegistrationNumber</p></td>
        <td><p>Any CRN that does not meet the validation rule</p></td>
        <td>
            <p>400 (Bad Request)</p>
            <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;Malformed CRN submitted&quot; }</p></td>
        </td>
    </tr>
    <tr>
        <td><p>Missing CorrelationId</p></td>
        <td><p>CorrelationId header is missing</p></td>
        <td>
            <p>400 (Bad Request)</p>
            <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;CorrelationId is required&quot; }</p></td>
        </td>
    </tr>
    <tr>
        <td><p>Malformed CorrelationId</p></td>
        <td><p>CorrelationId header is malformed</p></td>
        <td>
            <p>400 (Bad Request)</p>
            <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;Malformed CorrelationId&quot; }</p></td>
        </td>
    </tr>
  </tbody>
</table>
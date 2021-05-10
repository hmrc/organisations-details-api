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
        <td><p>Data found</p>
        <td><p>As above.</p></td>
        <td><p>200 (OK)</p><p>Payload as response example above</p></td>
    </tr>
    <tr>
          <td>
            <p>
              Missing payload
            </p>
          </td>
          <td><p>Missing payload</p></td>
          <td><p>400 (Bad Request)</p>
          <p>{ &quot;code&quot; : &quot;PAYLOAD_REQUIRED&quot;,<br/>&quot;message&quot; : &quot;Payload is required&quot; }</p></td>
    </tr>
    <tr>
        <td><p>Malformed payload</p></td>
        <td><p>Any payload that does not meet the validation rules</p></td>
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
            <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;Malformed CorrelationId&quot; }</p>
        </td>
    </tr>
  </tbody>
</table>
<div id="table_header">
    <div class="ticker">Ticker</div>
    <div class="amount">Amount</div>
    <div class="avg-buy-price">Avg buy price</div>
    <div class="value">Value</div>
    <div class="current-price">Current price</div>
    <div class="current-value">Current value</div>
    <div class="return">Return</div>
    <div class="sell"></div>
</div>
<div id="table_content">
    <#list shares>
        <#items as share>
            <div class="ticker">${share.ticker}</div>
            <div class="amount">${share.sharesTotalAmount?string["0"]}</div>
            <div class="avg-buy-price">${share.avgBuyPrice?string["###0.00##"]} PLN</div>
            <div class="value">${share.baseValue?string["###0.00"]} PLN</div>
            <div class="current-price">${share.currentPrice?string["###0.00##"]} PLN</div>
            <div class="current-value">${share.currentValue?string["###0.00"]} PLN</div>
            <div class="return">${share.currentReturn?string["###0.00"]} %</div>
            <div class="sell">
                <input id="sell_btn" name="ticker" type="button" onClick="location.href='/share-sell?ticker=${share.ticker}'" value="Report SELL">
            </div>
        </#items>
    </#list>
</div>
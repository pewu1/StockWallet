<div>
<form class="buy_form" method="POST" action="/share-buy">
    <div class="input_area">
        TICKER</br>
<#--        <input type="text"   name="ticker"  placeholder="e.g. KGH"/></div>-->

    <input type="text" class="text-input" autocomplete="off" required name="ticker" placeholder="Search by ticker or company name..." list="tickersList"/>

    <datalist id="tickersList">
            <#list tickers>
                <#items as ticker>
                    <option value="${ticker.tickerName}">${ticker.tickerName} - ${ticker.fullName}</option>
                </#items>
            </#list>
    </datalist>
    </div>
    <div class="input_area">
        AMOUNT</br>
        <input type="number" min="1" step="1" class="text-input" autocomplete="off" id="amount" required name="amount"/></div>
    <div class="input_area">
        PRICE IN PLN</br>
        <input type="number" min="0.0001" step="0.0001" class="text-input" autocomplete="off" id="price" required name="price"/></div>
    </br>
    <input id="buy" type="submit" value="Report BUY"/>
</form>
<div id="info">
    <span id="fee">Total price has fee included!</span></br>
    Available money: ${"${freeCash}"?replace(","," ")} PLN</br>
    Transaction: 0 PLN</br>
    <span>Money left: ${"${freeCash}"?replace(","," ")} PLN</span></br>
</div>
<div id="status"
     <#if status == "Transaction success">style="color: lightgreen"</#if>
        <#if status == "Amount should be a whole number">style="color: red"</#if>
        <#if status == "Amount should be an integer greater than 0">style="color: red"</#if>
        <#if status == "Price should have a numerical value - format 0.0000">style="color: red"</#if>
        <#if status == "Price should be a number greater than 0 - format 0.0000">style="color: red"</#if>
        <#if status == "You don't have enough money!">style="color: red"</#if>
        <#if status?ends_with("not valid") >style="color: red"</#if>
>${status}</div>
</div>
<script>
    $("#amount, #price").on('change keyup', function() {
        var money = parseFloat(${"${freeCash}"?replace(",","")});
        var amount = document.getElementById("amount").value;
        var price = document.getElementById("price").value;
        var totalPrice = parseInt(amount) * parseFloat(price);
        var totalPriceFeeIncluded = (totalPrice + (totalPrice*0.0039)).toFixed(2);
        var moneyLeft = money - totalPriceFeeIncluded;
        if (!isNaN(totalPrice)) {
            if(moneyLeft >= 0)
            document.getElementById("info").innerHTML = "<span id='fee'>Total price has fee included!</span> </br> " +
                "Available money: ${"${freeCash}"?replace(","," ")} PLN </br>" +
                "Transaction: -" + totalPriceFeeIncluded + " PLN </br>" +
                    "<span>Money left: " + moneyLeft.toFixed(2) + " PLN</span>";
            else{
                document.getElementById("info").innerHTML = "<span id='fee'>Total price has fee included!</span> </br> " +
                    "Available money: ${"${freeCash}"?replace(","," ")} PLN </br>" +
                    "Transaction: -" + totalPriceFeeIncluded + " PLN </br>" +
                    "<span style='color:red;'>Money left: " + moneyLeft.toFixed(2)+ " PLN</span>";
            }
        }
        else{
            document.getElementById("info").innerHTML = "<span id='fee'>Total price has fee included!</span> </br> " +
                "    Available money: ${"${freeCash}"?replace(","," ")} PLN </br> " +
                "    Transaction: 0 PLN</br>" +
                "    <span>Money left: ${"${freeCash}"?replace(","," ")} PLN</span></br>"
        }
    });
</script>
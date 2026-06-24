'use strict';

function formatTraderNumber(value) {
  var lang = document.documentElement.lang || 'en';
  return value.toLocaleString(lang, {minimumFractionDigits: 0, maximumFractionDigits: 0});
}

function updateTrader() {
  var table = $('#trader-table');
  if (table.length === 0) {
    return;
  }

  var tradedResource = table.attr('data-traded-resource');
  var tradedResourceRate = +table.attr('data-rate-' + tradedResource);
  var resources = ['metal', 'crystal', 'deuterium'];

  var total = 0.0;
  $('#metal-amount').val(0);
  $('#crystal-amount').val(0);
  $('#deuterium-amount').val(0);

  for (var i = 0; i < resources.length; i++) {
    var resource = resources[i];
    if (resource === tradedResource) {
      continue;
    }

    var input = $('#trade-' + resource);
    var amount = +input.val();
    if (!Number.isFinite(amount) || amount < 0) {
      amount = 0;
    }
    amount = Math.floor(amount);

    var resourceRate = +table.attr('data-rate-' + resource);
    var received = amount * (tradedResourceRate / resourceRate);
    total += received;
    $('#' + resource + '-amount').val(amount);
  }

  total = Math.floor(total * 100.0) / 100.0;
  $('#trade-total').text(formatTraderNumber(total));
  $('#expected-received-amount').val(total.toFixed(2));
}

$('.trader-input').on('input change', updateTrader);
updateTrader();

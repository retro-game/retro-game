'use strict';

$('#all-units').click(function () {
  $('[data-set-for^="unit_"][data-set-value!=""][data-set-value!="0"]').click();
  return false;
});

$('#no-units').click(function () {
  $('[id^="unit_"]').val('').trigger('change');
  return false;
});

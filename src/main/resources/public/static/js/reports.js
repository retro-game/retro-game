'use strict';

$('#delete-all').click(function () {
  const elem = $(this);
  const ok = confirm(elem.attr('data-msg'));
  if (ok)
    elem.parent('form').submit();
  return false;
});

$('[data-delete-report]').click(function () {
  var elem = $(this);
  var kind = elem.attr('data-kind');
  var bodyId = +elem.attr('data-body');
  var reportId = +elem.attr('data-id');
  $.ajax({
    type: 'post',
    url: '/reports/' + kind + '/delete',
    contentType: 'application/json',
    data: JSON.stringify({
      bodyId: bodyId,
      reportId: reportId
    }),
    success: function (data) {
      if (data.success) {
        let e = elem.parent().parent();
        if (kind === 'other') {
          let th = e.prev().prev();
          let msg = e.prev();
          th.remove();
          msg.remove();
          e.remove();
        } else {
          e.remove();
        }
      } else {
        alert('Deleting the report failed');
      }
    },
    error: function () {
      alert('Deleting the report failed');
    }
  });
});

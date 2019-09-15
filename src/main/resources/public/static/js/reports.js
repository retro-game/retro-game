'use strict';

$('#delete-all').click(function () {
  const ok = confirm('Do you want to delete all reports?');
  if (ok) {
    const elem = $(this);
    const body = +elem.attr('data-body');
    const kind = elem.attr('data-kind');
    $.ajax({
      type: 'post',
      url: '/reports/' + kind + '/delete-all',
      contentType: 'application/json',
      data: JSON.stringify({body}),
      success: function (data) {
        if (data.success) {
          location.reload();
        } else {
          alert('Deleting all reports failed');
        }
      },
      error: function () {
        alert('Deleting all reports failed');
      }
    });
  }
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
        var e = elem.parent().parent();
        if (kind === 'other') {
          e = e.parent();
        }
        e.remove();
      } else {
        alert('Deleting the report failed');
      }
    },
    error: function () {
      alert('Deleting the report failed');
    }
  });
});

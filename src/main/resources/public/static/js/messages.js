'use strict';

$('#delete-all').click(function () {
  const elem = $(this);
  const ok = confirm(elem.attr('data-msg'));
  if (ok)
    elem.parent('form').submit();
  return false;
});

$('[data-delete-message]').click(function () {
  const elem = $(this);
  const body = +elem.attr('data-body');
  const kind = elem.attr('data-kind');
  const message = +elem.attr('data-message');
  $.ajax({
    type: 'post',
    url: '/messages/private/delete',
    contentType: 'application/json',
    data: JSON.stringify({body, kind, message}),
    success: function (data) {
      if (data.success) {
        const actionsRow = elem.closest('tr');
        actionsRow.prev('tr').remove();
        actionsRow.prev('tr').remove();
        actionsRow.remove();
      } else {
        alert('Deleting the message failed');
      }
    },
    error: function () {
      alert('Deleting the message failed');
    }
  });
});

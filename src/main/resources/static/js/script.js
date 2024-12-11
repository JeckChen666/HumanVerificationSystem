$(document).ready(function() {
    loadQuestion();
});

function loadQuestion() {
    $.get('/getQuestion', function(data) {
        $('#questionText').text(data.questionText);
        $('#questionTextInput').val(data.questionText);
        $('#option1').text(data.options[0]).val(data.options[0]);
        $('#option2').text(data.options[1]).val(data.options[1]);
        $('#option3').text(data.options[2]).val(data.options[2]);
        $('#option4').text(data.options[3]).val(data.options[3]);
    });
}

function submitAnswer(button) {
    const answer = button.value;
    const questionText = $('#questionTextInput').val();

    $.post('/verify', { answer: answer, questionText: questionText }, function(data) {
        if (data.correct) {
            showNotification('Correct! Redirecting...', 'success');
            setTimeout(function() {
                window.location.href = data.redirectUrl;
            }, 1500);
        } else {
            showNotification('Incorrect! Please try again.', 'error');
            setTimeout(function() {
                loadQuestion();
            }, 1500);
        }
    });
}

function showNotification(message, type) {
    $('#notificationMessage').text(message);
    $('#notificationModal').removeClass('hidden').addClass('flex');
    setTimeout(function() {
        $('#notificationModal').removeClass('flex').addClass('hidden');
    }, 1500);
}
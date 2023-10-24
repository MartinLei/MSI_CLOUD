$(document).ready(function () {
    getData().then(() => {
        // Clear the data-display div before adding new content
        $('#data-display').empty();

        // Iterate through the items in the data and create HTML elements
        data.items.forEach(item => {
            const fileName = item.fileName;
            const contentType = item.contentType;
            const itemId = item.id;

            // Create a new row for each item
            const itemRow = $('<div class="row" style="padding: 5px">');

            // Create a column for fileName
            const fileNameColumn = $('<div class="col" >');
            fileNameColumn.html(`<p id="${fileName}"><strong>File Name:</strong> ${fileName}</p>`);

            // Create a column for contentType
            const contentTypeColumn = $('<div class="col">');
            contentTypeColumn.html(`<p><strong>Content Type:</strong> ${contentType}</p>`);

            // Create a column for buttons
            const buttonColumn = $('<div class="col">');

            // Create a download button
            const downloadButton = $('<button class="btn btn-primary" style="margin-right: 10px">Download</button>');
            downloadButton.on('click', function() {
                window.location.href = `/download/${itemId}`;
            });

            // Create a delete button
            const deleteButton = $('<button class="btn btn-danger" >Delete</button>');
            deleteButton.on('click', function() {
                // You can add confirmation logic here before sending the delete request
                // If you want to confirm, consider using a modal dialog.
                $.ajax({
                    method: "POST",
                    url: `/delete/${itemId}`,
                });
            });
            // Append the buttons to the button column
            buttonColumn.append(downloadButton);
            buttonColumn.append(deleteButton);

            // Append the columns to the row
            itemRow.append(fileNameColumn);
            itemRow.append(contentTypeColumn);
            itemRow.append(buttonColumn);

            // Append the item row to the data-display div
            $('#data-display').append(itemRow);
    });
});

let data = {};

function getData() {
    return $.ajax({
        method: "GET",
        url: "/files",
        dataType: "json",
        success: function (response) {
            data = response;
        }
    });
}

const triggerButton = document.getElementById('triggerButton');
const hiddenInput = document.getElementById('hiddenInput');

triggerButton.addEventListener('click', function() {
    // Trigger a click event on the hidden input
    hiddenInput.click();
});

const searchButton = document.getElementById('searchButton');

const inputText = document.getElementById('searchInput');

    searchButton.addEventListener('click', function () {

        let searchTerm = inputText.value;

        const pList = document.getElementsByClassName("row");
        const pArray = Array.from(pList)
        // resets all border styles
        pArray.forEach(pItem => {
            pItem.style.border = "none"
        })

        data.items.forEach(item => {
            const fileName = item.fileName;
            if(fileName === searchTerm) {
                let searchResult =document.getElementById(searchTerm).parentElement.parentElement;
                searchResult.style.borderStyle = "ridge";
                searchResult.style.borderColor = "red";
            }
        })
    });
})



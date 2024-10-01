document.addEventListener('DOMContentLoaded', function() {
    // Document is ready

    // Add an event listener to the range input
    document.getElementById('compressQuality').addEventListener('input', function() {
        // Update the value in the span element
        document.getElementById('rangeValue').innerText = this.value + "%";
    });


    /*
    document.getElementById('files').addEventListener('change', function(event) {
        // Get the selected files
        const files = event.target.files;

        // Clear previously generated inputs
        document.getElementById('height-div').innerHTML = '';

        // Generate inputs based on the number of selected files
        for (let i = 0; i < files.length; i++) {

            const label = document.createElement('label');
            label.className = 'form-label';
            label.textContent = 'Resize Height for image (' + files[i].name + ')';

            const input = document.createElement('input');
            input.type = 'number';
            input.className = 'form-control';
            input.name = 'heights[]';

            // Create an Image object to get the original height
            const img = new Image();
            img.src = URL.createObjectURL(files[i]);
            img.onload = function() {
                // Set the placeholder to the original height
                input.placeholder =  img.naturalHeight + ' pixels';
                input.value = img.naturalHeight;
            };

            document.getElementById('height-div').appendChild(label);
            document.getElementById('height-div').appendChild(input);
        }
    });*/

    // Fetch data from /api/image/format using the Fetch API
    fetch('/api/image/formats')
        .then(response => response.json())
        .then(data => {
            // Populate the select element with options
            const selectElement = document.getElementById('imageFormats');

            // Clear existing options
            selectElement.innerHTML = '';

            // Append options from the fetched data
            data.forEach(format => {
                const option = document.createElement('option');
                option.value = format;
                option.text = format.toUpperCase();
                selectElement.appendChild(option);
            });
        })
        .catch(error => console.error('Error fetching data:', error));
});

// Add these variables at the top of your script
let applyToAllCheckbox = document.getElementById('applyToAll');
let globalWidthInput = document.getElementById('globalWidth');
let globalHeightInput = document.getElementById('globalHeight');

// Add this function to handle checkbox change
applyToAllCheckbox.addEventListener('change', function() {
    globalWidthInput.style.display = this.checked ? 'inline-block' : 'none';
    globalHeightInput.style.display = this.checked ? 'inline-block' : 'none';
    if (!this.checked) {
        globalWidthInput.value = '';
        globalHeightInput.value = '';
        // Reset all width and height inputs to their original values
        resetDimensions();
    }
});

// Add this function to handle global width input change
globalWidthInput.addEventListener('input', function() {
    if (applyToAllCheckbox.checked) {
        let newWidth = parseInt(this.value);
        if (isNaN(newWidth) || newWidth <= 0) return;
        updateAllDimensions(newWidth, null);
    }
});

// Add this function to handle global height input change
globalHeightInput.addEventListener('input', function() {
    if (applyToAllCheckbox.checked) {
        let newHeight = parseInt(this.value);
        if (isNaN(newHeight) || newHeight <= 0) return;
        updateAllDimensions(null, newHeight);
    }
});

// Function to update all dimensions
function updateAllDimensions(newWidth, newHeight) {
    const widthInputs = document.getElementsByName('width[]');
    const heightInputs = document.querySelectorAll('[id^="height"]');

    for (let i = 0; i < widthInputs.length; i++) {
        const aspectRatio = parseFloat(widthInputs[i].dataset.aspectRatio);
        if (newWidth) {
            widthInputs[i].value = newWidth;
            heightInputs[i].value = Math.round(newWidth / aspectRatio);
        } else if (newHeight) {
            heightInputs[i].value = newHeight;
            widthInputs[i].value = Math.round(newHeight * aspectRatio);
        }
    }
}

// Function to reset dimensions to original values
function resetDimensions() {
    const widthInputs = document.getElementsByName('width[]');
    const heightInputs = document.querySelectorAll('[id^="height"]');

    for (let i = 0; i < widthInputs.length; i++) {
        const originalWidth = widthInputs[i].placeholder.split(' ')[0];
        widthInputs[i].value = originalWidth;
        const aspectRatio = parseFloat(widthInputs[i].dataset.aspectRatio);
        heightInputs[i].value = Math.round(originalWidth / aspectRatio);
    }
}

// Modify the existing updateFileList function
function updateFileList() {
    const fileListElement = document.getElementById('fileList');
    const files = document.getElementById('files').files;

    // Clear existing table contents including header
    fileListElement.innerHTML = '';

    // Create table head and its row
    const tableHead = document.createElement('thead');
    const tableHeadRow = tableHead.insertRow();

    // Insert the header cells and set their content
    const headerCell1 = tableHeadRow.insertCell(0);
    headerCell1.textContent = 'Filename';

    const headerCell2 = tableHeadRow.insertCell(1);
    headerCell2.textContent = 'Resized width (in pixels)';

    const headerCell3 = tableHeadRow.insertCell(2);
    headerCell3.textContent = 'Resized height (in pixels)';

    // Append the table head to the table
    fileListElement.appendChild(tableHead);

    // Create table body and append after the header
    const tableBody = document.createElement('tbody');
    fileListElement.appendChild(tableBody);

    if (files.length > 50) {
        alert('You can only select up to 50 files.');
        return;
    }

    for (let i = 0; i < files.length; i++) {
        const row = tableBody.insertRow();
        const cell1 = row.insertCell(0);
        cell1.textContent = files[i].name;
        const cell2 = row.insertCell(1);

        const img = new Image();
        img.src = URL.createObjectURL(files[i]);
        img.onload = function() {
            const aspectRatio = img.naturalWidth / img.naturalHeight;
            console.log("width " + img.naturalWidth + " : " + img.naturalHeight);

            // Insert width input field
            cell2.innerHTML = `
            <input type="number" class="form-control width-input" name="width[]" 
                id="width${i}" placeholder="${img.naturalWidth} pixels" value="${img.naturalWidth}" data-aspect-ratio="${aspectRatio}">`;

            // Create a new cell for the height input (not editable, just showing calculated height)
            const cell3 = row.insertCell(2);
            cell3.innerHTML = `
            <input type="number" class="form-control" id="height${i}" value="${img.naturalHeight}" readonly>`;

            // Add event listener to the width input to recalculate the height on change
            const widthInput = document.getElementById(`width${i}`);
            const heightInput = document.getElementById(`height${i}`);

            widthInput.addEventListener('input', function () {
                const newWidth = this.value;
                const newHeight = Math.round(newWidth / aspectRatio);
                heightInput.value = newHeight;
            });

            // Check if "Apply to all" is checked and update accordingly
            if (applyToAllCheckbox.checked) {
                const globalWidth = globalWidthInput.value;
                if (globalWidth) {
                    widthInput.value = globalWidth;
                    heightInput.value = Math.round(globalWidth / aspectRatio);
                }
            }
        };
    }
}

function convertImage() {

    // Disable the button
    $('#btn-convert-image').prop('disabled', true);

    // Get form values
    const format = document.getElementById('imageFormats').value;
    const compressQuality = document.getElementById('compressQuality').value;
    const filesInput = document.getElementById('files');
    const stripMetadata = document.getElementById('stripMetadata').checked;  // Get checkbox value

    // Check file sizes before proceeding
    const maxFileSize = 100 * 1024 * 1024; // 100MB in bytes
    for (const file of filesInput.files) {
        if (file.size > maxFileSize) {
            alert('One or more files are too large. Please select files smaller than 100MB.');
            $("#btn-convert-image").prop('disabled', false); // Re-enable the button
            return; // Stop the function
        }
    }

    // Create a FormData object to send form data
    const formData = new FormData();
    formData.append('format', format);
    formData.append('compressQuality', compressQuality);
    formData.append('stripMetadata', stripMetadata);  // Append the checkbox value

    // Append files to FormData
    for (const file of filesInput.files) {
        formData.append('files', file);
    }

    // Get all elements with the name 'widths[]'
    const widthInputs = document.getElementsByName('width[]');

    // Loop through the NodeList and append values to FormData
    for (let i = 0; i < widthInputs.length; i++) {
        const width = widthInputs[i].value;
        formData.append('widths[]', width);
    }

    // Determine which API to call based on the number of files selected
    filesInput.files.length > 1 ? multiConversion(formData) : singleConversion(formData);

    // Re-enable the button after 10 seconds
    setTimeout(() => {
        $('#btn-convert-image').prop('disabled', false);
    }, 3000); // 3 seconds
}

function singleConversion(formData){

    // Make a request to /api/image/upload
    fetch('/api/image/uploadSingle', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            // Check if the request was successful
            if (!response.ok) {
                alert('Image conversion failed');
            }
            // Return the blob (binary) data
            return response.blob();
        })
        .then(blobData => {

            console.log("blob = " + JSON.stringify(blobData,null,2));
            console.log("blob is instance of " + blobData instanceof Blob)
            console.log("blob data size " + blobData.size);
            if(blobData instanceof Blob) {
                // Create a data URL from the blob data
                const imageUrl = URL.createObjectURL(blobData);

                // Display the resulting image in the "result" div
                const resultDiv = document.getElementById('result');
                resultDiv.innerHTML = ''; // Clear previous content

                // Create an img element and set its source to the data URL of the resulting image
                const img = document.createElement('img');
                img.src = imageUrl;

                // Create a link element for downloading the image
                const downloadLink = document.createElement('a');
                downloadLink.href = imageUrl;
                downloadLink.download = 'processed-image.' + formData.get('format'); // You can customize the default filename

                // Attach a double-click event listener to the image
                img.addEventListener('dblclick', function () {
                    // Append the link to the body
                    document.body.appendChild(downloadLink);

                    // Trigger a click on the link to initiate the download
                    downloadLink.click();

                    // Remove the link from the body
                    document.body.removeChild(downloadLink);
                });

                resultDiv.appendChild(img);
                const span = document.createElement('span');
                span.innerHTML="Double click to download the image";
                span.style.color="red";
                span.style.fontSize="12px";
                span.style.marginTop="5px";
                span.style.marginLeft="auto";
                span.style.marginRight="auto";
                resultDiv.appendChild(span);
            }
        })
        .catch(error => alert('Error converting image:' + error));
}

function multiConversion(formData){

    // Make a request to /api/image/upload
    fetch('/api/image/thread/uploadMulti', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            // Check if the request was successful
            if (!response.ok) {
                alert('Image conversion failed');
            }
            // Return the blob (binary) data
            return response.arrayBuffer();
        })
        .then(data => {
            if (data instanceof ArrayBuffer) {
                // Handle zip file response
                // Create a Blob from the ArrayBuffer and initiate download
                const blob = new Blob([data], { type: 'application/zip' });

                // Display the resulting image in the "result" div
                const downloadDiv = document.getElementById('downloadDiv');
                const downloadLink = document.createElement('a');
                downloadLink.href = URL.createObjectURL(blob);
                downloadLink.download = 'processed_images.zip';
                downloadDiv.appendChild(downloadLink);
                downloadLink.click();
                downloadDiv.removeChild(downloadLink);
            }})
        .catch(error => alert('Error converting image:' + error));
}
console.log("hello js");

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
                option.text = format;
                selectElement.appendChild(option);
            });
        })
        .catch(error => console.error('Error fetching data:', error));
});

function convertImage() {

    // Disable the button
    $('#btn-convert-image').prop('disabled', true);

    // Get form values
    const format = document.getElementById('imageFormats').value;
    const compressQuality = document.getElementById('compressQuality').value;
    const filesInput = document.getElementById('files');

    // Create a FormData object to send form data
    const formData = new FormData();
    formData.append('format', format);
    formData.append('compressQuality', compressQuality);

    // Append files to FormData
    for (const file of filesInput.files) {
        formData.append('files', file);
    }

    // Get all elements with the name 'heights[]'
    const heightInputs = document.getElementsByName('heights[]');

    // Loop through the NodeList and append values to FormData
    for (let i = 0; i < heightInputs.length; i++) {
        const height = heightInputs[i].value;
        formData.append('heights[]', height);
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
    headerCell2.textContent = 'Resize height (in pixels)';

    // Append the table head to the table
    fileListElement.appendChild(tableHead);

    // Create table body and append after the header
    const tableBody = document.createElement('tbody');
    fileListElement.appendChild(tableBody);

    if (files.length > 10) {
        alert('You can only select up to 10 files.');
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
            console.log("height " + i + ": " + img.naturalHeight);
            cell2.innerHTML =
                `<input type="number" class="form-control" name="heights[]" ` +
                `id="height${i}" placeholder="${img.naturalHeight} pixels" ` +
                `value="${img.naturalHeight}">`
        };
    }
}

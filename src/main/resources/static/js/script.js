document.addEventListener('DOMContentLoaded', function() {
    // Document is ready

    // Add an event listener to the range input
    document.getElementById('compressQuality').addEventListener('input', function() {
        // Update the value in the span element
        document.getElementById('rangeValue').innerText = this.value + "%";
    });

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

    // Add these variables at the top of your script
    let applyToAllCheckbox = document.getElementById('applyToAll');
    let globalWidthInput = document.getElementById('globalWidth');

    // Add this function to handle checkbox change
    applyToAllCheckbox.addEventListener('change', function() {
        globalWidthInput.style.display = this.checked ? 'inline-block' : 'none';
        if (!this.checked) {
            globalWidthInput.value = '';
            // Reset all width and height inputs to their original values
            resetDimensions();
        }
    });

    // Add this function to handle global width input change
    globalWidthInput.addEventListener('input', function() {
        if (applyToAllCheckbox.checked) {
            let newWidth = parseInt(this.value);
            if (isNaN(newWidth) || newWidth <= 0) return;
            updateAllDimensions(newWidth);
        }
    });

    // Function to update all dimensions
    function updateAllDimensions(newWidth) {
        const widthInputs = document.querySelectorAll('input[name^="width_"]');
        const heightInputs = document.querySelectorAll('input[name^="height_"]');

        widthInputs.forEach((widthInput, index) => {
            const aspectRatio = parseFloat(widthInput.dataset.aspectRatio);
            widthInput.value = newWidth;
            heightInputs[index].value = Math.round(newWidth / aspectRatio);
        });
    }

    // Function to reset dimensions to original values
    function resetDimensions() {
        const widthInputs = document.querySelectorAll('input[name^="width_"]');
        const heightInputs = document.querySelectorAll('input[name^="height_"]');

        widthInputs.forEach((widthInput, index) => {
            const originalWidth = widthInput.placeholder.split(' ')[0];
            widthInput.value = originalWidth;
            const aspectRatio = parseFloat(widthInput.dataset.aspectRatio);
            heightInputs[index].value = Math.round(originalWidth / aspectRatio);
        });
    }

    // Function to update the file list and preview images
    function updateFileList() {
        const fileList = document.getElementById('fileList').getElementsByTagName('tbody')[0];
        fileList.innerHTML = ''; // Clear existing rows

        const files = document.getElementById('files').files;
        console.log("Files selected:", files); // Debugging log
        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const row = fileList.insertRow();

            // Filename column with preview
            const filenameCell = row.insertCell(0);
            const img = document.createElement('img');
            img.src = URL.createObjectURL(file);
            img.style.width = '50px';
            img.style.height = '50px';
            img.style.marginRight = '10px';
            filenameCell.appendChild(img);
            filenameCell.appendChild(document.createTextNode(file.name));

            // Resized (width in pixel) column
            const widthCell = row.insertCell(1);
            const widthInput = document.createElement('input');
            widthInput.type = 'number';
            widthInput.className = 'form-control';
            widthInput.name = `width_${i}`;
            widthInput.style.width = '100px';
            widthCell.appendChild(widthInput);

            // Resized (height in pixel) column
            const heightCell = row.insertCell(2);
            const heightInput = document.createElement('input');
            heightInput.type = 'number';
            heightInput.className = 'form-control';
            heightInput.name = `height_${i}`;
            heightInput.style.width = '100px';
            heightCell.appendChild(heightInput);

            // Read image dimensions
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = new Image();
                img.onload = function() {
                    console.log("Image dimensions:", img.width, img.height); // Debugging log
                    widthInput.value = img.width;
                    heightInput.value = img.height;
                    widthInput.dataset.aspectRatio = img.width / img.height;
                };
                img.src = e.target.result;
            };
            reader.readAsDataURL(file);
        }
    }

    // Attach the updateFileList function to the file input change event
    document.getElementById('files').addEventListener('change', updateFileList);

    // Attach the convertImage function to the button click event
    document.getElementById('btn-convert-image').addEventListener('click', convertImage);
});


function convertImage() {
    // Disable the button to prevent multiple submissions
    document.getElementById('btn-convert-image').disabled = true;

    // Get form values
    const format = document.getElementById('imageFormats').value;
    const compressQuality = document.getElementById('compressQuality').value;
    const filesInput = document.getElementById('files');
    const stripMetadata = document.getElementById('stripMetadata').checked;

    // Create a FormData object to send form data
    const formData = new FormData();
    formData.append('format', format);
    formData.append('compressQuality', compressQuality);
    formData.append('stripMetadata', stripMetadata);

    // Append files to FormData
    for (const file of filesInput.files) {
        formData.append('files', file);
    }

    // Get all elements with the name 'widths[]'
    const widthInputs = document.querySelectorAll('input[name^="width_"]');

    // Loop through the NodeList and append values to FormData
    widthInputs.forEach((widthInput, index) => {
        const width = widthInput.value;
        formData.append('widths[]', width);
    });

    // Determine which API to call based on the number of files selected
    if (filesInput.files.length > 1) {
        multiConversion(formData);
    } else {
        singleConversion(formData);
    }

    // Re-enable the button after 10 seconds
    setTimeout(() => {
        document.getElementById('btn-convert-image').disabled = false;
    }, 10000); // 10 seconds
}


function singleConversion(formData) {
    // Make a request to /api/image/uploadSingle
    fetch('/api/image/uploadSingle', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Image conversion failed');
        }
        return response.blob();
    })
    .then(blobData => {
        const imageUrl = URL.createObjectURL(blobData);
        const resultDiv = document.getElementById('result');
        resultDiv.innerHTML = ''; // Clear previous content

        const img = document.createElement('img');
        img.src = imageUrl;

        const downloadLink = document.createElement('a');
        downloadLink.href = imageUrl;
        downloadLink.download = 'processed-image.' + formData.get('format');

        img.addEventListener('dblclick', function() {
            document.body.appendChild(downloadLink);
            downloadLink.click();
            document.body.removeChild(downloadLink);
        });

        resultDiv.appendChild(img);
        const span = document.createElement('span');
        span.innerHTML = "Double click to download the image";
        span.style.color = "red";
        span.style.fontSize = "12px";
        span.style.marginTop = "5px";
        span.style.marginLeft = "auto";
        span.style.marginRight = "auto";
        resultDiv.appendChild(span);
    })
    .catch(error => alert('Error converting image: ' + error));
}

function multiConversion(formData) {
    // Make a request to /api/image/uploadMulti
    fetch('/api/image/uploadMulti', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Image conversion failed');
        }
        return response.arrayBuffer();
    })
    .then(data => {
        const blob = new Blob([data], { type: 'application/zip' });
        const downloadDiv = document.getElementById('downloadDiv');
        const downloadLink = document.createElement('a');
        downloadLink.href = URL.createObjectURL(blob);
        downloadLink.download = 'processed_images.zip';
        downloadDiv.appendChild(downloadLink);
        downloadLink.click();
        downloadDiv.removeChild(downloadLink);
    })
    .catch(error => alert('Error converting image: ' + error));
}
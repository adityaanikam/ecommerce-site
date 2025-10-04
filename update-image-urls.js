const fs = require('fs');
const path = require('path');

// Read the products JSON file
const productsData = JSON.parse(fs.readFileSync('ecommerce.products.json', 'utf8'));

// Base URL for serving images
const BASE_IMAGE_URL = 'http://localhost:8080/products';

// Function to get the correct image extension for a product
function getImageExtension(productName, category) {
  const productsPath = path.join(__dirname, 'backend', 'Products', category, productName);
  
  if (fs.existsSync(productsPath)) {
    const files = fs.readdirSync(productsPath);
    const imageFile = files.find(file => file.startsWith('1.'));
    if (imageFile) {
      return path.extname(imageFile);
    }
  }
  
  // Default to .jpg if not found
  return '.jpg';
}

// Function to update image URLs for a product
function updateProductImages(product) {
  const category = product.category;
  const productName = product.name;
  
  // Get the correct image extension
  const extension = getImageExtension(productName, category);
  
  // Create the new image URLs
  const newImages = [
    `${BASE_IMAGE_URL}/${category}/${productName.replace(/\s+/g, '+')}/1${extension}`,
    `${BASE_IMAGE_URL}/${category}/${productName.replace(/\s+/g, '+')}/2${extension}`,
    `${BASE_IMAGE_URL}/${category}/${productName.replace(/\s+/g, '+')}/3${extension}`
  ];
  
  return {
    ...product,
    images: newImages
  };
}

// Update all products
const updatedProducts = productsData.map(updateProductImages);

// Write the updated data back to the file
fs.writeFileSync('ecommerce.products.json', JSON.stringify(updatedProducts, null, 2));

console.log('✅ Updated image URLs for all products');
console.log(`📊 Updated ${updatedProducts.length} products`);

// Show some examples
console.log('\n📋 Sample updated products:');
updatedProducts.slice(0, 3).forEach(product => {
  console.log(`\n${product.name}:`);
  product.images.forEach((image, index) => {
    console.log(`  ${index + 1}. ${image}`);
  });
});

const { MongoClient } = require('mongodb');
const fs = require('fs');
const path = require('path');

// MongoDB Atlas connection string
const MONGODB_URI = process.env.SPRING_DATA_MONGODB_URI || process.env.MONGODB_URI || 'mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/ecommerce?retryWrites=true&w=majority';

async function uploadProductsToAtlas() {
    const client = new MongoClient(MONGODB_URI);
    
    try {
        await client.connect();
        console.log('Connected to MongoDB Atlas');
        
        const db = client.db('ecommerce');
        const products = db.collection('products');
        
        // Read the JSON file
        const jsonPath = path.join(__dirname, '..', 'ecommerce.products.json');
        const jsonData = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));
        
        console.log(`Found ${jsonData.length} products in JSON file`);
        
        // Clear existing products
        await products.deleteMany({});
        console.log('Cleared existing products');
        
        // Insert all products
        const result = await products.insertMany(jsonData);
        console.log(`Successfully inserted ${result.insertedCount} products`);
        
        // Verify the upload
        const count = await products.countDocuments();
        console.log(`Total products in database: ${count}`);
        
        // Show sample product
        const sampleProduct = await products.findOne({});
        if (sampleProduct) {
            console.log('Sample product:', {
                id: sampleProduct._id,
                name: sampleProduct.name,
                category: sampleProduct.category,
                images: sampleProduct.images
            });
        }
        
    } catch (error) {
        console.error('Error:', error);
    } finally {
        await client.close();
    }
}

// Run the script
if (require.main === module) {
    uploadProductsToAtlas();
}

module.exports = { uploadProductsToAtlas };

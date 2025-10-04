// Direct MongoDB update script
const { MongoClient } = require('mongodb');

async function updateImages() {
    const client = new MongoClient('mongodb://localhost:27017');
    
    try {
        await client.connect();
        console.log('Connected to MongoDB');
        
        const db = client.db('ecommerce');
        const products = db.collection('products');
        
        // Update all products with correct image paths
        const result = await products.updateMany(
            {}, // Update all documents
            {
                $set: {
                    images: [
                        "/products/Electronics/Airpods Pro 2/1.webp",
                        "/products/Electronics/Airpods Pro 2/2.webp", 
                        "/products/Electronics/Airpods Pro 2/3.webp"
                    ]
                }
            }
        );
        
        console.log(`Updated ${result.modifiedCount} products`);
        
    } catch (error) {
        console.error('Error:', error);
    } finally {
        await client.close();
    }
}

updateImages();

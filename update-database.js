// Simple script to update MongoDB with correct image paths
const { MongoClient } = require('mongodb');

async function updateImagePaths() {
    const client = new MongoClient('mongodb://localhost:27017');
    
    try {
        await client.connect();
        console.log('Connected to MongoDB');
        
        const db = client.db('ecommerce');
        const products = db.collection('products');
        
        // Get all products
        const allProducts = await products.find({}).toArray();
        console.log(`Found ${allProducts.length} products`);
        
        let updatedCount = 0;
        
        for (const product of allProducts) {
            const category = product.category;
            const name = product.name;
            
            // Create the correct image paths
            const imagePaths = [
                `/products/${category}/${name}/1.webp`,
                `/products/${category}/${name}/2.webp`,
                `/products/${category}/${name}/3.webp`
            ];
            
            // Update the product
            await products.updateOne(
                { _id: product._id },
                { 
                    $set: { 
                        images: imagePaths,
                        updatedAt: new Date().getTime()
                    } 
                }
            );
            
            console.log(`Updated ${product.name}: ${imagePaths[0]}`);
            updatedCount++;
        }
        
        console.log(`\n✅ Successfully updated ${updatedCount} products`);
        
    } catch (error) {
        console.error('Error:', error);
    } finally {
        await client.close();
    }
}

updateImagePaths();

// MongoDB initialization script
db = db.getSiblingDB('ecommerce');

// Create collections with validation
db.createCollection('users', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['email', 'password', 'firstName', 'lastName', 'username'],
      properties: {
        email: {
          bsonType: 'string',
          pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        },
        username: {
          bsonType: 'string',
          minLength: 3
        },
        firstName: {
          bsonType: 'string',
          minLength: 1
        },
        lastName: {
          bsonType: 'string',
          minLength: 1
        }
      }
    }
  }
});

db.createCollection('products', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['name', 'price', 'category'],
      properties: {
        name: {
          bsonType: 'string',
          minLength: 1
        },
        price: {
          bsonType: 'decimal'
        },
        category: {
          bsonType: 'string',
          minLength: 1
        }
      }
    }
  }
});

db.createCollection('categories');
db.createCollection('carts');
db.createCollection('orders');

// Create indexes for better performance
db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ username: 1 }, { unique: true });

db.products.createIndex({ name: 'text', description: 'text' });
db.products.createIndex({ category: 1 });
db.products.createIndex({ brand: 1 });
db.products.createIndex({ price: 1 });
db.products.createIndex({ sku: 1 }, { unique: true });

db.categories.createIndex({ name: 1 }, { unique: true });
db.categories.createIndex({ parentCategoryId: 1 });

db.carts.createIndex({ userId: 1 }, { unique: true });

db.orders.createIndex({ userId: 1 });
db.orders.createIndex({ orderNumber: 1 }, { unique: true });
db.orders.createIndex({ status: 1 });
db.orders.createIndex({ createdAt: -1 });

print('Database initialization completed successfully!');

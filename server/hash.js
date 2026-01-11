const bcrypt = require('bcrypt');

const password1 = 'password1';
const password2 = 'password2';
const password3 = 'password3';

bcrypt.hash(password1, 10, (err, hash1) => {
  if (err) throw err;
  console.log(`Hashed password1: ${hash1}`);
  
  bcrypt.hash(password2, 10, (err, hash2) => {
    if (err) throw err;
    console.log(`Hashed password2: ${hash2}`);

    bcrypt.hash(password3, 10, (err, hash3) => {
      if (err) throw err;
      console.log(`Hashed password3: ${hash3}`);
    });
  });
});

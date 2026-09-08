export const ANIMALS = [
  { id: 'cat', label: 'Cats', service: 'placecats.com' },
  { id: 'dog', label: 'Dogs', service: 'place.dog' },
  { id: 'bear', label: 'Bears', service: 'placebear.com' },
];

export const ANIMAL_IDS = ANIMALS.map((animal) => animal.id);

const SMALLEST = 300;
const SPREAD = 200;

const randomDimension = () => SMALLEST + Math.floor(Math.random() * SPREAD);

export function animalImageUrl(animal) {
  return `/animals/${animal}/${randomDimension()}/${randomDimension()}`;
}

export function labelFor(animal) {
  return ANIMALS.find(({ id }) => id === animal)?.label ?? animal;
}

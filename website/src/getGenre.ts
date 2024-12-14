export function getGenre(genres: string[]) {
    for (const genre of ['metal', 'rock', 'punk']) {
        if (genres.find((it) => it.toLowerCase().includes(genre))) {
            return genre;
        }
    }

    return 'unknown';
}

import math
import re
import os
import pprint
import numpy as np
import json
import matplotlib.pyplot as plt

english_alphabet_to_frequency = {
    'a': .08167,
    'b': .01492,
    'c': .02782,
    'd': .04253,
    'e': .12702,
    'f': .0228,
    'g': .02015,
    'h': .06094,
    'i': .06966,
    'j': .00153,
    'k': .00772,
    'l': .04025,
    'm': .02406,
    'n': .06749,
    'o': .07507,
    'p': .01929,
    'q': .00095,
    'r': .05987,
    's': .06327,
    't': .09056,
    'u': .02758,
    'v': .00978,
    'w': .0236,
    'x': .0015,
    'y': .01974,
    'z': .00074
}

english_alphabet = list(english_alphabet_to_frequency.keys())

test_text_lengths = [500, 1000, 2000]
test_kw_lengths = [3, 4, 5, 6]
l_gram_lengths = [3, 4, 5, 6]
keywords = ["fat", "word", "seven", "cotton"]


def remove_not_allowed_chars(text: str, alphabet: list):
    text = text.lower()
    text = re.sub(r'[\n\t\-,.]', ' ', text)
    for symbol in text:
        if symbol not in alphabet and symbol != ' ':
            text = text.replace(symbol, '')
    text = re.sub(r'\s+', ' ', text)
    return text


# text is allowed to include only characters from alphabet and whitespaces
def encrypt_by_vigener(text: str, keyword: str, alphabet: list):
    keyword = keyword.lower()
    shifts = []
    for symbol in keyword:
        shifts.append(ord(symbol) - ord(alphabet[0]))
    _encrypted_text = ''
    i = 0
    for text_symbol in text:
        if text_symbol not in alphabet:
            _encrypted_text += text_symbol
            i += 1
            continue
        _encrypted_text += alphabet[(alphabet.index(text_symbol) + shifts[i % len(shifts)]) % len(alphabet)]
        i += 1
    print(_encrypted_text)
    return _encrypted_text


def decrypt_vigener(text: str, shifts: list, alphabet: list):
    decrypted_text = ''
    i = 0
    for text_symbol in text:
        if text_symbol in alphabet:
            decrypted_text += alphabet[alphabet.index(text_symbol) - shifts[i % len(shifts)]]
        else:
            decrypted_text += text_symbol
        i += 1
    return decrypted_text


def find_distances_between_l_grams(text: str, l):
    l_gram_last_positions = {}
    _distances = []
    for i in range(len(text) - l + 1):
        l_gram = text[i:i + l]
        if l_gram in l_gram_last_positions.keys():
            distance = i - l_gram_last_positions.get(l_gram)
            if distance not in _distances:
                _distances.append(distance)
        l_gram_last_positions[l_gram] = i
    return _distances


def gcd(_distances: list):
    if len(_distances) == 0:
        print('Distances list is empty!')
        return
    _gcd = _distances[0]
    for value in _distances[1:len(_distances)]:
        _gcd = math.gcd(_gcd, value)
    return _gcd


def split_text(text: str, _kw_length):
    mono_texts = []
    for i in range(_kw_length):
        mono_texts.append(text[i:len(text):_kw_length])
    return mono_texts


def find_shift(mono_text: str, alphabet: dict):
    letter_occurrences = {}
    length = len(mono_text.replace(' ', ''))
    alphabet_letters = list(alphabet.keys())
    for letter in mono_text:
        if letter == ' ':
            continue
        if letter in letter_occurrences:
            letter_occurrences[letter] += 1
        else:
            letter_occurrences[letter] = 1

    for key in letter_occurrences.keys():
        letter_occurrences[key] = letter_occurrences[key] / length

    for letter in alphabet_letters:
        if letter not in letter_occurrences.keys():
            letter_occurrences[letter] = 0

    shift = 0
    min_distance = float("inf")
    for i in range(0, len(alphabet_letters)):
        distance = 0
        for letter in alphabet_letters:
            index_in_alphabet = alphabet_letters.index(letter) - i
            distance += (letter_occurrences[letter] - alphabet[alphabet_letters[index_in_alphabet]]) ** 2
        if distance < min_distance:
            min_distance = distance
            shift = i
    return shift


if not os.path.exists("encrypted"):
    os.mkdir("encrypted")

if not os.path.exists("decrypted"):
    os.mkdir("decrypted")

# encrypting source texts
for length in test_text_lengths:
    for test_no in range(1, 11):
        for keyword in keywords:
            print("Text Len: " + str(length) + "; Encryption No: " + str(test_no) + "; Keyword: " + keyword)
            with open("source_texts/src_" + str(length) + "_" + str(test_no) + ".txt", "r") as file:
                source_text = str(file.read())
                encrypted_text = encrypt_by_vigener(source_text, keyword, english_alphabet)
                with open("encrypted/enc_" + str(length) + "_" + str(test_no) + "_" + keyword + ".txt",
                          "w") as enc_file:
                    enc_file.write(encrypted_text)

stats = {}
stages = ["kw_length", "decryption"]

# create dicts for writing decryption stats
for text_length in test_text_lengths:
    stats[text_length] = {}
    for kw in keywords:
        stats[text_length][kw] = {}
        for i in range(3, 9):
            stats[text_length][kw][i] = {}
            for stage in stages:
                stats[text_length][kw][i][stage] = 0

# decryption
for length in test_text_lengths:
    for keyword in keywords:
        for test_no in range(1, 11):
            with open("encrypted/enc_" + str(length) + "_" + str(test_no) + "_" + keyword + ".txt", "r") as enc_file:
                print("Text len: {}; Keyword: {}; Text No: {}".format(length, keyword, test_no))
                encrypted_text = str(enc_file.read())
                print("Encrypted text:")
                print(encrypted_text)
                for l in range(3, 9):
                    print("Testing with l-grams of lenfth " + str(l))
                    print("____________________________________________")
                    distances = find_distances_between_l_grams(encrypted_text, l)
                    print("Distances: {}".format(distances))
                    kw_length = gcd(distances)
                    print("GCD: {}".format(kw_length))

                    if kw_length != len(keyword):
                        continue
                    stats[length][keyword][l]["kw_length"] += 0.1

                    mono_texts = split_text(encrypted_text, kw_length)
                    shifts = []
                    for mono_text in mono_texts:
                        shifts.append(find_shift(mono_text, english_alphabet_to_frequency))
                    found_keyword = ""
                    for shift in shifts:
                        found_keyword += chr(ord(english_alphabet[0]) + shift)
                    print("Found keyword: {}".format(found_keyword))

                    if found_keyword != keyword:
                        continue
                    stats[length][keyword][l]["decryption"] += 0.1

                    decrypted_text = decrypt_vigener(encrypted_text, shifts, english_alphabet)
                    print("Decrypted text:")
                    print(decrypted_text)
                    with open("decrypted/decr_" + str(length) + "_" + str(test_no) + "_" + keyword + "_l-" + str(l) +
                              ".txt", "w") as decr_file:
                        decr_file.write(decrypted_text)

print(json.dumps(stats, sort_keys=False, indent=4))


def build_text_len_coordinates(_stats: dict, l):
    coords = []
    for text_length in test_text_lengths:
        y = 0
        for keyword in keywords:
            y += _stats[text_length][keyword][l]["decryption"]
        y /= len(keywords)
        coords.append(y)
    return coords


def build_keyword_len_coords(_stats: dict, l):
    kw_stats = {}
    for keyword in keywords:
        kw_stats[keyword] = 0

    for text_length in test_text_lengths:
        for keyword in keywords:
            kw_stats[keyword] += _stats[text_length][keyword][l]["decryption"]

    coords = []
    for value in kw_stats.values():
        coords.append(value / len(test_text_lengths))
    return coords


# for l in range(3, 9):
#     x = test_text_lengths
#     y = build_text_len_coordinates(stats, l)
#
#     plt.suptitle('L-gram len: ' + str(l), fontsize=20)
#     plt.plot(x, y, 'ro')
#     plt.xlabel('Text length', fontsize=12)
#     plt.ylabel('% of success', fontsize=12)
#     # plt.xticks(np.arange(min(text_length), max(text_length) + 200, 200))
#     # plt.yticks(np.arange(0.0, 1.1, 0.1))
#     plt.axis([0, test_text_lengths[2] + 100, -0.1, 1.1])
#     plt.show()


for l in range(3, 9):
    x = [len(keyword) for keyword in keywords]
    y = build_keyword_len_coords(stats, l)

    plt.suptitle('L-gram len: ' + str(l), fontsize=20)
    plt.plot(x, y, 'ro')
    plt.xlabel('Keyword length', fontsize=12)
    plt.ylabel('% of success', fontsize=12)
    # plt.xticks(np.arange(min(text_length), max(text_length) + 200, 200))
    # plt.yticks(np.arange(0.0, 1.1, 0.1))
    plt.axis([x[0] - 1, x[len(x) - 1] + 1, -0.1, 1.1])
    plt.show()
# pprint.pprint(stats)

# source_text = remove_not_allowed_chars(source_text, english_alphabet)
# print(source_text)
# encrypted_text = encrypt_by_vigener(source_text, 'MOUSE', english_alphabet)
# print(encrypted_text)
# distances = find_distances_between_l_grams(encrypted_text, 4)
# print(find_distances_between_l_grams(encrypted_text, 4))
# print(gcd(distances))

# old_lengths = [200, 800, 1600]
# new_file_length_index = 0
# new_file_index = 1
# for length in old_lengths:
#     for i in range(1, 11):
#         with open("./old/src_" + str(length) + "_" + str(i) + ".txt", "r") as file:
#             content = remove_not_allowed_chars(str(file.read()), list(english_alphabet.keys()))
#             print(len(content))
#             current_len = test_text_lengths[new_file_length_index]
#             while current_len < len(content):
#                 part = content[current_len - test_text_lengths[new_file_length_index]:current_len]
#                 print(len(part))
#                 with open("./source_texts/src_" + str(test_text_lengths[new_file_length_index]) + "_" + str(
#                         new_file_index) + ".txt", "w") as new_file:
#                     new_file.write(part)
#                     if new_file_index < 10:
#                         new_file_index += 1
#                     elif new_file_length_index < len(test_text_lengths):
#                         new_file_index = 0
#                         new_file_length_index += 1
#                     else:
#                         raise Exception("Finished")
#                 current_len += test_text_lengths[new_file_length_index]
#
# new_file_length_index = 2
# new_file_index = 2
# with open("src.txt", "r") as file:
#     content = remove_not_allowed_chars(str(file.read()), list(english_alphabet.keys()))
#     print(len(content))
#     current_len = test_text_lengths[new_file_length_index]
#     while current_len < len(content):
#         part = content[current_len - test_text_lengths[new_file_length_index]:current_len]
#         print(len(part))
#         with open("./source_texts/src_" + str(test_text_lengths[new_file_length_index]) + "_" + str(
#                 new_file_index) + ".txt", "w") as new_file:
#             new_file.write(part)
#             if new_file_index < 10:
#                 new_file_index += 1
#             elif new_file_length_index < len(test_text_lengths):
#                 new_file_index = 0
#                 new_file_length_index += 1
#             else:
#                 raise Exception("Finished")
#         current_len += test_text_lengths[new_file_length_index]
